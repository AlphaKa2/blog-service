package com.alphaka.blogservice.tag.service;

import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
import com.alphaka.blogservice.tag.dto.TagListResponse;
import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.tag.entity.PostTag;
import com.alphaka.blogservice.tag.entity.Tag;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.tag.repository.PostTagRepository;
import com.alphaka.blogservice.tag.repository.TagRepository;
import com.alphaka.blogservice.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final CacheUtils cacheUtils;
    private final UserClient userClient;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final BlogRepository blogRepository;

    /**
     * 블로그에 등록된 태그 목록 조회
     * @param nickname - 블로그 주인의 닉네임
     * @return List<TagListResponse> - 태그 목록과 목록별 게시글 수
     */
    @Cacheable(value = "blogService:tagList", key = "@postService.getBlogIdByNickname(nickname)", unless = "#result == null || #result.isEmpty()")
    public List<TagListResponse> getTagListForBlog(String nickname) {
        log.info("블로그의 태그 목록 조회 시작 - Nickname: {}", nickname);

        // 요청 받은 닉네임의 사용자 ID 조회
        UserDTO user = userClient.findUserByNickname(nickname).getData();
        if (user == null) {
            log.error("사용자를 찾을 수 없음 - Nickname: {}", nickname);
            throw new UserNotFoundException();
        }

        // 해당 사용자의 블로그 조회
        Blog blog = blogRepository.findById(user.getUserId()).orElseThrow(BlogNotFoundException::new);

        // 해당 블로그에 등록된 태그 목록 조회
        List<Tag> tags = postTagRepository.findTagsByBlogId(blog.getId());

        // 태그별 게시글 수 계산하여 태그 목록과 함께 반환 (해당 블로그의 게시글로 한정)
        List<TagListResponse> tagList = tags.stream()
                .map(tag -> {
                    // 특정 블로그에서 해당 태그가 달린 게시글 수 계산
                    int postCount = postTagRepository.countByBlogIdAndTagId(blog.getId(), tag.getId());
                    return new TagListResponse(tag.getTagName(), postCount);
                })
                .toList();

        log.info("블로그의 태그 목록 조회 완료 - Nickname: {}", nickname);
        return tagList;
    }

    /**
     * 게시글 생성시 태그 추가
     * @param post - 게시글
     * @param tagNames - 태그 목록
     */
    @Transactional
    public void addTagsToPost(Post post, List<String> tagNames) {
        log.info("게시글 태그 추가 요청 - Post ID: {}", post.getId());

        if (tagNames == null || tagNames.isEmpty()) {
            log.info("추가할 태그가 없습니다.");
            return;
        }

        // 태그를 조회하거나 없으면 생성
        List<Tag> tags = findOrCreateTags(tagNames);

        List<PostTag> postTags = tags.stream()
                .map(tag -> PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build())
                .toList();

        postTagRepository.batchInsert(postTags);
        log.info("게시글 생성 시 태그 추가 완료 - Post ID: {}", post.getId());

        // 태그 추가 시, 관련 캐시 무효화 (블로그의 태그 목록)
        cacheUtils.evictTagListCache(post.getBlog().getId());
    }

    /**
     * 게시글의 태그를 조회
     * @param postId - 게시글 ID
     * @return List<String> - 태그 목록
     */
    public List<String> findTagsByPostId(Long postId) {
        log.info("게시글의 태그 조회 - Post ID: {}", postId);
        return postTagRepository.findTagsByPostId(postId);
    }

    /**
     * 게시글 ID 리스트로 태그 조회
     * @param postIds - 게시글 ID 리스트
     * @return Map<Long, List<String>> - 게시글 ID 별 태그 목록
     */
    public Map<Long, List<String>> findTagsByPostIds(List<Long> postIds) {
        log.info("게시글 ID로 태그 조회 - Post IDs: {}", postIds);
        return tagRepository.findTagsByPostIds(postIds);
    }

    /**
     * 게시글 업데이트 시 태그 정보를 업데이트
     * @param post - 게시글
     * @param tagNames - 태그명 목록
     */
    @Transactional
    public void updateTagsForPost(Post post, List<String> tagNames) {
        log.info("태그 업데이트 시작 - Post ID: {}", post.getId());

        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }

        // 현재 게시글에 매핑된 태그 가져오기
        Set<String> newTagNamesSet = new HashSet<>(tagNames);
        Set<String> currentTagNames = postTagRepository.findByPost(post).stream()
                .map(postTag -> postTag.getTag().getTagName())
                .collect(Collectors.toSet());

        // 추가할 태그와 제거할 태그 식별
        Set<String> tagsToAdd = determineTagsToAdd(newTagNamesSet, currentTagNames);
        Set<String> tagsToRemove = determineTagsToRemove(newTagNamesSet, currentTagNames);

        // 태그 추가
        if (!tagsToAdd.isEmpty()) {
            handleTagsToAdd(post, tagsToAdd);
        }

        // 태그 제거
        if (!tagsToRemove.isEmpty()) {
            handleTagsToRemove(post, tagsToRemove);
        }

        log.info("게시글 업데이트 시 태그 처리 완료 - Post ID: {}", post.getId());

        // 태그 업데이트 시, 관련 캐시 무효화 (블로그의 태그 목록)
        cacheUtils.evictTagListCache(post.getBlog().getId());
    }

    /**
     * 태그 존재 여부를 확인하고 없으면 생성
     * @param tagNames - 태그명 목록
     * @return List<Tag> - 추가할 태그 목록
     */
    private List<Tag> findOrCreateTags(List<String> tagNames) {
        // 기존 태그 조회
        List<Tag> existingTags = new ArrayList<>(tagRepository.findByTagNameIn(tagNames));
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());
        log.info("존재하는 태그명: {}", existingTagNames);

        // 새로 생성해야 할 태그 이름 추출
        List<String> newTagNames = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .collect(Collectors.toList());
        log.info("새로 생성해야 하는 태그명: {}", newTagNames);

        // 새로운 태그 생성 및 삽입
        if (!newTagNames.isEmpty()) {
            List<Tag> newTags = newTagNames.stream()
                    .map(tagName -> Tag.builder().tagName(tagName).build())
                    .collect(Collectors.toList());
            tagRepository.batchInsert(newTags);

            // 배치 삽입된 태그를 재조회하여 id 값을 가져옴
            List<Tag> insertedTags = tagRepository.findByTagNameIn(newTagNames);
            existingTags.addAll(insertedTags);
            log.info("새로 삽입된 태그: {}", insertedTags);
        }

        return existingTags;
    }

    /**
     * 추가할 태그를 결정
     * @param newTags - 새로운 태그명
     * @param currentTags - 현재 태그명
     * @return Set<String> - 추가할 태그명
     */
    private Set<String> determineTagsToAdd(Set<String> newTags, Set<String> currentTags) {
        Set<String> tagsToAdd = new HashSet<>(newTags);
        tagsToAdd.removeAll(currentTags);
        log.debug("Tags to add: {}", tagsToAdd);
        return tagsToAdd;
    }

    /**
     * 제거할 태그를 결정
     * @param newTags - 새로운 태그명
     * @param currentTags - 현재 태그명
     * @return Set<String> - 제거할 태그명
     */
    private Set<String> determineTagsToRemove(Set<String> newTags, Set<String> currentTags) {
        Set<String> tagsToRemove = new HashSet<>(currentTags);
        tagsToRemove.removeAll(newTags);
        log.debug("Tags to remove: {}", tagsToRemove);
        return tagsToRemove;
    }

    /**
     * 태그를 추가
     * @param post - 게시글
     * @param tagsToAdd - 추가할 태그명
     */
    private void handleTagsToAdd(Post post, Set<String> tagsToAdd) {
        log.info("태그 추가 - Post ID: {}, Tags: {}", post.getId(), tagsToAdd);
        List<Tag> tagsAdded = findOrCreateTags(new ArrayList<>(tagsToAdd));
        List<PostTag> newPostTags = tagsAdded.stream()
                .map(tag -> PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build())
                .toList();
        postTagRepository.batchInsert(newPostTags);
        log.info("태그 추가 완료 - Post ID: {}, Tags: {}", post.getId(), tagsAdded);
    }

    /**
     * 태그를 제거
     * @param post - 게시글
     * @param tagsToRemove - 제거할 태그명
     */
    private void handleTagsToRemove(Post post, Set<String> tagsToRemove) {
        log.info("태그 제거 - Post ID: {}, Tags: {}", post.getId(), tagsToRemove);
        List<PostTag> postTagsToRemove = postTagRepository.findByPostAndTag_TagNameIn(post, new ArrayList<>(tagsToRemove));
        postTagRepository.deleteAllInBatch(postTagsToRemove);
        log.info("태그 제거 완료 - Post ID: {}, Tags: {}", post.getId(), tagsToRemove);
    }
}
