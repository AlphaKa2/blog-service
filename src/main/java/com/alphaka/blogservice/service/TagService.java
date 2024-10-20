package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.dto.request.UserInfo;
import com.alphaka.blogservice.dto.response.BlogTagListResponse;
import com.alphaka.blogservice.entity.Blog;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.entity.PostTag;
import com.alphaka.blogservice.entity.Tag;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.repository.BlogRepository;
import com.alphaka.blogservice.repository.PostTagRepository;
import com.alphaka.blogservice.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final UserClient userClient;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final BlogRepository blogRepository;

    /**
     * 블로그에 등록된 태그 목록 조회
     * @param nickname 블로그 주인의 닉네임
     * @return 태그 목록과 목록별 게시글 수
     */
    public List<BlogTagListResponse> getTagListForBlog(String nickname) {
        log.info("블로그의 태그 목록 조회 시작 - Nickname: {}", nickname);

        // 요청 받은 닉네임의 사용자 ID 조회
        UserInfo user = userClient.findUserByNickname(nickname).getData();

        // 해당 사용자의 블로그 조회
        Blog blog = blogRepository.findById(user.getUserId()).orElseThrow(BlogNotFoundException::new);

        // 해당 블로그에 등록된 태그 목록 조회
        List<Tag> tags = postTagRepository.findTagsByBlogId(blog.getId());

        // 태그별 게시글 수 계산하여 태그 목록과 함께 반환 (해당 블로그의 게시글로 한정)
        List<BlogTagListResponse> tagList = tags.stream()
                .map(tag -> {
                    // 특정 블로그에서 해당 태그가 달린 게시글 수 계산
                    int postCount = postTagRepository.countByBlogIdAndTagId(blog.getId(), tag.getId());
                    return new BlogTagListResponse(tag.getTagName(), postCount);
                })
                .toList();

        log.info("블로그의 태그 목록 조회 완료 - Nickname: {}", nickname);
        return tagList;
    }

    /**
     * 게시글 생성 시 태그를 처리
     * @param post 게시글
     * @param tagNames 태그 이름 목록
     */
    @Transactional
    public void addTagsToNewPost(Post post, List<String> tagNames) {
        log.info("게시글 생성 시 태그 추가 시작 - Post ID: {}", post.getId());

        // 태그를 조회하거나 없으면 생성
        List<Tag> tags = findOrCreateTags(tagNames);

        // PostTag 엔티티 리스트 생성
        List<PostTag> postTags = tags.stream()
                .map(tag -> PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build())
                .toList();

        // PostTag를 배치로 저장
        postTagRepository.batchInsert(postTags);

        log.info("게시글 생성 시 태그 추가 완료 - Post ID: {}", post.getId());
    }

    /**
     * 태그 이름 목록을 조회하거나 없으면 생성
     * @param tagNames 태그 이름 목록
     * @return 태그 엔티티 목록
     */
    private List<Tag> findOrCreateTags(List<String> tagNames) {
        // 주어진 태그 이름으로 기존 태그 조회
        List<Tag> existingTags = tagRepository.findByTagNameIn(tagNames);

        // 기존 태그 이름 목록 추출
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());

        // 새로 생성해야 하는 태그 이름 목록
        List<Tag> newTags = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .map(tagName -> Tag.builder().tagName(tagName).build())
                .toList();

        // 새로운 태그를 배치로 저장
        if (!newTags.isEmpty()) {
            tagRepository.batchInsert(newTags);
            existingTags.addAll(newTags);
        }

        return existingTags;
    }

    /**
     * 게시글 업데이트 시 태그 정보를 업데이트
     * @param post 게시글
     * @param tagNames 태그 이름 목록
     */
    @Transactional
    public void updateTagsForExistingPost(Post post, List<String> tagNames) {
        log.info("게시글 업데이트 시 태그 처리 시작 - Post ID: {}", post.getId());

        // 현재 게시글에 매핑된 태그 가져오기
        List<PostTag> existingPostTags = postTagRepository.findByPost(post);
        List<String> existingTagNames = existingPostTags.stream()
                .map(postTag -> postTag.getTag().getTagName())
                .toList();

        // 추가할 태그와 제거할 태그 식별
        List<String> tagsToAdd = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .toList();

        List<String> tagsToRemove = existingTagNames.stream()
                .filter(existingTagName -> !tagNames.contains(existingTagName))
                .toList();

        // 태그 추가
        if (!tagsToAdd.isEmpty()) {
            addTagsToNewPost(post, tagsToAdd);
        }

        // 태그 제거
        if (!tagsToRemove.isEmpty()) {
            removeTagsFromPost(post, tagsToRemove);
        }

        log.info("게시글 업데이트 시 태그 처리 완료 - Post ID: {}", post.getId());
    }

    /**
     * 게시글에서 태그를 제거
     * @param post 게시글
     * @param tagNames 제거할 태그 이름 목록
     */
    @Transactional
    public void removeTagsFromPost(Post post, List<String> tagNames) {
        log.info("게시글의 태그 제거 시작 - Post ID: {}", post.getId());

        List<PostTag> postTagsToRemove = postTagRepository.findByPostAndTag_TagNameIn(post, tagNames);
        postTagRepository.deleteAllInBatch(postTagsToRemove);

        log.info("게시글의 태그 제거 완료 - Post ID: {}", post.getId());
    }
}
