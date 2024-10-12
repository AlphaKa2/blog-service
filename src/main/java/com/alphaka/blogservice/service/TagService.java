package com.alphaka.blogservice.service;

import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.entity.PostTag;
import com.alphaka.blogservice.entity.Tag;
import com.alphaka.blogservice.repository.PostTagRepository;
import com.alphaka.blogservice.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    /**
     * 게시글의 태그 정보를 업데이트
     *
     * @param post     게시글
     * @param tagNames 태그 목록
     */
    @Transactional
    public void updateTagsForPost(Post post, List<String> tagNames) {
        log.info("게시글의 태그 업데이트 시작 - Post ID: {}", post.getId());

        // 현재 게시글에 매핑된 태그 가져오기
        List<PostTag> existingPostTags = postTagRepository.findByPost(post);
        List<String> existingTagNames = existingPostTags.stream()
                .map(postTag -> postTag.getTag().getTagName())
                .toList();

        // 추가할 태그와 제거할 태그 식별
        List<String> tagsToAdd = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .toList();

        List<String> tagsToRemove = tagNames.stream()
                .filter(tagName -> !tagNames.contains(tagName))
                .toList();

        // 게시글에 태그 추가
        addTagsToPost(post, tagsToAdd);

        // 게시글에서 태그 제거
        removeTagsFromPost(post, tagsToRemove);

        log.info("게시글의 태그 업데이트 완료 - Post ID: {}", post.getId());
    }

    /**
     * 게시글에 태그 추가
     * @param post 게시글
     * @param tagNames 태그 목록
     */
    @Transactional
    public void addTagsToPost(Post post, List<String> tagNames) {
        log.info("게시글에 태그 추가 시작 - Post ID: {}", post.getId());

        // 태그를 조회하고 없으면 생성
        List<Tag> tags = findOrCreateTags(tagNames);

        // 게시글에 태그 추가
        for (Tag tag : tags) {
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();
            postTagRepository.save(postTag);
        }

        log.info("게시글에 태그 추가 완료 - Post ID: {}", post.getId());
    }

    /**
     * 게시글에서 태그를 제거
     * @param post 게시글
     * @param tagNames 제거할 태그 목록
     */
    @Transactional
    public void removeTagsFromPost(Post post, List<String> tagNames) {
        log.info("게시글의 태그 제거 - Post ID: {}", post.getId());

        List<PostTag> postTagsToRemove = postTagRepository.findByPostAndTag_TagNameIn(post, tagNames);
        postTagRepository.deleteAll(postTagsToRemove);

        log.info("게시글의 태그 제거 완료 - Post ID: {}", post.getId());
    }

    /**
     * 태그 이름 목록을 조회하거나 없으면 생성
     *
     * @param tagNames 태그 이름 목록
     * @return 태그 목록
     */
    private List<Tag> findOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .tagName(tagName)
                                .build();
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }
}
