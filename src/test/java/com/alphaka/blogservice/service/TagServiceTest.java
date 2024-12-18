package com.alphaka.blogservice.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.tag.dto.TagListResponse;
import com.alphaka.blogservice.tag.entity.PostTag;
import com.alphaka.blogservice.tag.entity.Tag;
import com.alphaka.blogservice.tag.repository.PostTagRepository;
import com.alphaka.blogservice.tag.repository.TagRepository;
import com.alphaka.blogservice.tag.service.TagService;
import com.alphaka.blogservice.util.CacheUtils;
import com.alphaka.blogservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private CacheUtils cacheUtils;

    @Mock
    private UserClient userClient;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private TagService tagService;

    private UserDTO userDTO;
    private Blog blog;
    private Post post;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(
                1L,
                "tester",
                "profile.png"
        );

        blog = Blog.builder()
                .userId(userDTO.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        post = Post.builder()
                .userId(userDTO.getUserId())
                .blog(blog)
                .title("Test Post")
                .content("Test Content")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", 1L);
    }

    @Test
    @DisplayName("블로그에 등록된 태그 목록 조회 성공 - 태그 있음")
    void getTagListForBlog_success() {
        // given
        String nickname = "tester";
        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findById(userDTO.getUserId())).thenReturn(Optional.of(blog));

        Tag tag1 = Tag.builder().tagName("Java").build();
        TestUtil.setField(tag1, "id", 1L);
        Tag tag2 = Tag.builder().tagName("Spring").build();
        TestUtil.setField(tag2, "id", 2L);
        List<Tag> tags = Arrays.asList(tag1, tag2);
        when(postTagRepository.findTagsByBlogId(blog.getId())).thenReturn(tags);

        when(postTagRepository.countByBlogIdAndTagId(blog.getId(), tag1.getId())).thenReturn(3);
        when(postTagRepository.countByBlogIdAndTagId(blog.getId(), tag2.getId())).thenReturn(4);

        // when
        List<TagListResponse> tagList = tagService.getTagListForBlog(nickname);

        // then
        assertThat(tagList).hasSize(2);
        assertThat(tagList).extracting("tagName").containsExactlyInAnyOrder("Java", "Spring");
        assertThat(tagList).extracting("postCount").containsExactlyInAnyOrder(3, 4);

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findById(userDTO.getUserId());
        verify(postTagRepository, times(1)).findTagsByBlogId(blog.getId());
        verify(postTagRepository, times(1)).countByBlogIdAndTagId(blog.getId(), tag1.getId());
        verify(postTagRepository, times(1)).countByBlogIdAndTagId(blog.getId(), tag2.getId());
    }

    @Test
    @DisplayName("블로그에 등록된 태그 목록 조회 성공 - 태그 없음")
    void getTagListForBlog_success_noTags() {
        // given
        String nickname = "tester";
        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findById(userDTO.getUserId())).thenReturn(Optional.of(blog));
        when(postTagRepository.findTagsByBlogId(blog.getId())).thenReturn(List.of());

        // when
        List<TagListResponse> tagList = tagService.getTagListForBlog(nickname);

        // then
        assertThat(tagList).isEmpty();

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findById(userDTO.getUserId());
        verify(postTagRepository, times(1)).findTagsByBlogId(blog.getId());
    }

    @Test
    @DisplayName("블로그에 등록된 태그 목록 조회 실패 - 블로그 없음")
    void getTagListForBlog_fail_blogNotFound() {
        // given
        String nickname = "tester";
        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findById(userDTO.getUserId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagService.getTagListForBlog(nickname))
                .isInstanceOf(BlogNotFoundException.class);

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findById(userDTO.getUserId());
        verifyNoInteractions(postTagRepository);
    }

    @Test
    @DisplayName("블로그에 등록된 태그 목록 조회 실패 - 사용자 없음")
    void getTagListForBlog_fail_userNotFound() {
        // given
        String nickname = "nonexistent";
        when(userClient.findUserByNickname(nickname)).thenReturn(new com.alphaka.blogservice.common.response.ApiResponse<>(null));

        // when & then
        assertThatThrownBy(() -> tagService.getTagListForBlog(nickname))
                .isInstanceOf(UserNotFoundException.class);

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, never()).findById(anyLong());
        verify(postTagRepository, never()).findTagsByBlogId(anyLong());
        verify(cacheUtils, never()).evictTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글에 태그 추가 성공 - 기존 태그 존재, 새로운 태그 추가")
    void addTagsToPost_success_mixedTags() {
        // given
        List<String> tagNames = Arrays.asList("Java", "Spring", "JPA");
        Tag existingTag = Tag.builder().tagName("Java").build();
        TestUtil.setField(existingTag, "id", 1L);

        // 수정 가능한 리스트로 반환
        when(tagRepository.findByTagNameIn(tagNames)).thenReturn(new ArrayList<>(Collections.singletonList(existingTag)));

        Tag newTag1 = Tag.builder().tagName("Spring").build();
        Tag newTag2 = Tag.builder().tagName("JPA").build();

        // 새로운 태그를 삽입한 후 ID 설정
        doAnswer(invocation -> {
            List<Tag> tagsToInsert = invocation.getArgument(0);
            tagsToInsert.forEach(tag -> TestUtil.setField(tag, "id", (long) tag.getTagName().hashCode()));
            return null;
        }).when(tagRepository).batchInsert(anyList());

        // 새로운 태그 조회
        when(tagRepository.findByTagNameIn(Arrays.asList("Spring", "JPA"))).thenReturn(Arrays.asList(newTag1, newTag2));

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.addTagsToPost(post, tagNames);

        // then
        verify(tagRepository, times(1)).findByTagNameIn(tagNames);
        verify(tagRepository, times(1)).batchInsert(anyList());
        verify(tagRepository, times(1)).findByTagNameIn(Arrays.asList("Spring", "JPA"));
        verify(postTagRepository, times(1)).batchInsert(anyList());

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());

        verify(postTagRepository).batchInsert(postTagCaptor.capture());
        List<PostTag> savedPostTags = postTagCaptor.getValue();
        assertThat(savedPostTags).hasSize(3);
        assertThat(savedPostTags).extracting("tag.tagName").containsExactlyInAnyOrder("Java", "Spring", "JPA");
    }

    @Test
    @DisplayName("게시글에 태그 추가 성공 - 모든 태그는 기존 태그")
    void addTagsToPost_success_allExistingTags() {
        // given
        List<String> tagNames = Arrays.asList("Java", "Spring");

        Tag tag1 = Tag.builder().tagName("Java").build();
        TestUtil.setField(tag1, "id", 1L);
        Tag tag2 = Tag.builder().tagName("Spring").build();
        TestUtil.setField(tag2, "id", 2L);

        when(tagRepository.findByTagNameIn(tagNames)).thenReturn(Arrays.asList(tag1, tag2));

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.addTagsToPost(post, tagNames);

        // then
        verify(tagRepository, times(1)).findByTagNameIn(tagNames);
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, times(1)).batchInsert(postTagCaptor.capture());
        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());

        List<PostTag> savedPostTags = postTagCaptor.getValue();
        assertThat(savedPostTags).hasSize(2);
        assertThat(savedPostTags).extracting("tag.tagName").containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("게시글에 태그 추가 성공 - 모든 태그 새로 생성")
    void addTagsToPost_success_allNewTags() {
        // given
        List<String> tagNames = Arrays.asList("Java", "Spring");

        // 태그 조회 시 빈 리스트 반환
        when(tagRepository.findByTagNameIn(tagNames)).thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(
                        Tag.builder().tagName("Java").build(),
                        Tag.builder().tagName("Spring").build()
                )); // 재조회 시 새 태그 반환

        // 새로운 태그를 삽입한 후 ID 설정
        doAnswer(invocation -> {
            List<Tag> tagsToInsert = invocation.getArgument(0);
            tagsToInsert.forEach(tag -> TestUtil.setField(tag, "id", (long) tag.getTagName().hashCode()));
            return null;
        }).when(tagRepository).batchInsert(anyList());

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.addTagsToPost(post, tagNames);

        // then
        verify(tagRepository, times(2)).findByTagNameIn(tagNames);
        verify(tagRepository, times(1)).batchInsert(anyList());
        verify(postTagRepository, times(1)).batchInsert(postTagCaptor.capture());
        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());

        List<PostTag> savedPostTags = postTagCaptor.getValue();
        assertThat(savedPostTags).hasSize(2);
        assertThat(savedPostTags).extracting("tag.tagName").containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("게시글에 태그 추가 실패 - 태그 이름 목록이 null")
    void addTagsToPost_fail_nullTagNames() {
        // given
        List<String> tagNames = null;

        // when
        tagService.addTagsToPost(post, tagNames);

        // then
        verify(tagRepository, never()).findByTagNameIn(anyList());
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, never()).batchInsert(anyList());

        verify(cacheUtils, never()).evictTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글에 태그 추가 실패 - 태그 이름 목록이 비어있음")
    void addTagsToPost_fail_emptyTagNames() {
        // given
        List<String> tagNames = Collections.emptyList();

        // when
        tagService.addTagsToPost(post, tagNames);

        // then
        verify(tagRepository, never()).findByTagNameIn(anyList());
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, never()).batchInsert(anyList());

        verify(cacheUtils, never()).evictTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글의 태그 조회 성공 - 태그가 존재하는 경우")
    void findTagsByPostId_success_withTags() {
        // given
        Long postId = post.getId();
        List<String> tags = Arrays.asList("Java", "Spring");
        when(postTagRepository.findTagsByPostId(postId)).thenReturn(tags);

        // when
        List<String> result = tagService.findTagsByPostId(postId);

        // then
        assertThat(result).containsExactlyInAnyOrder("Java", "Spring");
        verify(postTagRepository, times(1)).findTagsByPostId(postId);
    }

    @Test
    @DisplayName("게시글의 태그 조회 성공 - 태그가 존재하지 않는 경우")
    void findTagsByPostId_success_noTags() {
        // given
        Long postId = post.getId();
        when(postTagRepository.findTagsByPostId(postId)).thenReturn(Collections.emptyList());

        // when
        List<String> result = tagService.findTagsByPostId(postId);

        // then
        assertThat(result).isEmpty();
        verify(postTagRepository, times(1)).findTagsByPostId(postId);
    }

    @Test
    @DisplayName("게시글 ID 리스트로 태그 조회 성공")
    void findTagsByPostIds_success() {
        // given
        List<Long> postIds = Arrays.asList(1L, 2L);
        Map<Long, List<String>> expectedMap = new HashMap<>();
        expectedMap.put(1L, Arrays.asList("Java", "Spring"));
        expectedMap.put(2L, Arrays.asList("Docker"));

        when(tagRepository.findTagsByPostIds(postIds)).thenReturn(expectedMap);

        // when
        Map<Long, List<String>> result = tagService.findTagsByPostIds(postIds);

        // then
        assertThat(result).isEqualTo(expectedMap);
        verify(tagRepository, times(1)).findTagsByPostIds(postIds);
    }

    @Test
    @DisplayName("게시글 태그 업데이트 성공 - 태그 추가 및 제거")
    void updateTagsForPost_success_addAndRemoveTags() {
        // given
        List<String> currentTags = Arrays.asList("Java", "Spring");
        List<String> newTags = Arrays.asList("Spring", "Docker");

        when(postTagRepository.findByPost(post)).thenReturn(Arrays.asList(
                createPostTag("Java", 1L),
                createPostTag("Spring", 2L)
        ));

        Tag tagJava = createTag("Java", 1L);
        Tag tagSpring = createTag("Spring", 2L);
        Tag tagDocker = createTag("Docker", 3L);

        // 새로운 태그 조회 (handleTagsToAdd 호출 시) - 첫 번째 호출: 빈 리스트, 두 번째 호출: [Docker]
        when(tagRepository.findByTagNameIn(Collections.singletonList("Docker")))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(tagDocker));

        // 새로운 태그 추가 및 ID 설정
        doAnswer(invocation -> {
            List<Tag> tagsToInsert = invocation.getArgument(0);
            tagsToInsert.forEach(tag -> TestUtil.setField(tag, "id", (long) tag.getTagName().hashCode()));
            return null;
        }).when(tagRepository).batchInsert(anyList());

        // 태그 제거할 PostTag 객체 설정 (handleTagsToRemove 호출 시)
        PostTag postTagJava = createPostTag("Java", 1L);
        when(postTagRepository.findByPostAndTag_TagNameIn(post, Collections.singletonList("Java"))).thenReturn(Collections.singletonList(postTagJava));

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.updateTagsForPost(post, newTags);

        // then
        verify(postTagRepository, times(1)).findByPost(post);
        verify(tagRepository, times(2)).findByTagNameIn(Collections.singletonList("Docker"));
        verify(tagRepository, times(1)).batchInsert(anyList());
        verify(postTagRepository, times(1)).batchInsert(postTagCaptor.capture());
        verify(postTagRepository, times(1)).findByPostAndTag_TagNameIn(post, Collections.singletonList("Java"));
        verify(postTagRepository, times(1)).deleteAllInBatch(Collections.singletonList(postTagJava));

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());

        List<PostTag> savedPostTags = postTagCaptor.getValue();
        assertThat(savedPostTags).hasSize(1);
        assertThat(savedPostTags.get(0).getTag().getTagName()).isEqualTo("Docker");
    }

    @Test
    @DisplayName("게시글 태그 업데이트 성공 - 태그 추가만 함")
    void updateTagsForPost_success_addOnlyTags() {
        // given
        List<String> currentTags = Arrays.asList("Java");
        List<String> newTags = Arrays.asList("Java", "Spring");

        when(postTagRepository.findByPost(post)).thenReturn(Arrays.asList(
                createPostTag("Java", 1L)
        ));

        Tag tagSpring = createTag("Spring", 2L);

        // findByTagNameIn(["Spring"]) 첫 번째 호출: 빈 리스트 (새로운 태그 필요)
        // findByTagNameIn(["Spring"]) 두 번째 호출: [Spring] (태그 생성 후)
        when(tagRepository.findByTagNameIn(Collections.singletonList("Spring")))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(tagSpring));

        // 새로운 태그 추가 및 ID 설정
        doAnswer(invocation -> {
            List<Tag> tagsToInsert = invocation.getArgument(0);
            tagsToInsert.forEach(tag -> TestUtil.setField(tag, "id", (long) tag.getTagName().hashCode()));
            return null;
        }).when(tagRepository).batchInsert(anyList());

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.updateTagsForPost(post, newTags);

        // then
        verify(postTagRepository, times(1)).findByPost(post);
        verify(tagRepository, times(2)).findByTagNameIn(Collections.singletonList("Spring"));
        verify(tagRepository, times(1)).batchInsert(anyList());
        verify(postTagRepository, times(1)).batchInsert(postTagCaptor.capture());
        verify(postTagRepository, never()).findByPostAndTag_TagNameIn(any(Post.class), anyList());
        verify(postTagRepository, never()).deleteAllInBatch(anyList());

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());

        List<PostTag> savedPostTags = postTagCaptor.getValue();
        assertThat(savedPostTags).hasSize(1);
        assertThat(savedPostTags.get(0).getTag().getTagName()).isEqualTo("Spring");
    }

    @Test
    @DisplayName("게시글 태그 업데이트 성공 - 태그 제거만 함")
    void updateTagsForPost_success_removeOnlyTags() {
        // given
        List<String> currentTags = Arrays.asList("Java", "Spring");
        List<String> newTags = Collections.singletonList("Java");

        when(postTagRepository.findByPost(post)).thenReturn(Arrays.asList(
                createPostTag("Java", 1L),
                createPostTag("Spring", 2L)
        ));

        // 태그 제거할 PostTag 객체 설정
        PostTag postTagSpring = createPostTag("Spring", 2L);
        when(postTagRepository.findByPostAndTag_TagNameIn(post, Collections.singletonList("Spring"))).thenReturn(Collections.singletonList(postTagSpring));

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.updateTagsForPost(post, newTags);

        // then
        verify(postTagRepository, times(1)).findByPost(post);
        verify(tagRepository, never()).findByTagNameIn(anyList());
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, times(1)).findByPostAndTag_TagNameIn(post, Collections.singletonList("Spring"));
        verify(postTagRepository, times(1)).deleteAllInBatch(Collections.singletonList(postTagSpring));

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());
    }

    @Test
    @DisplayName("게시글 태그 업데이트 성공 - 태그 목록이 null인 경우")
    void updateTagsForPost_success_nullTagNames() {
        // given
        List<String> newTags = null;

        when(postTagRepository.findByPost(post)).thenReturn(Arrays.asList(
                createPostTag("Java", 1L)
        ));

        // 태그 제거할 PostTag 객체 설정
        PostTag postTagJava = createPostTag("Java", 1L);
        when(postTagRepository.findByPostAndTag_TagNameIn(post, Collections.singletonList("Java"))).thenReturn(Collections.singletonList(postTagJava));

        ArgumentCaptor<List<PostTag>> postTagCaptor = ArgumentCaptor.forClass(List.class);

        // when
        tagService.updateTagsForPost(post, newTags);

        // then
        verify(postTagRepository, times(1)).findByPost(post);
        verify(tagRepository, never()).findByTagNameIn(anyList());
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, times(1)).findByPostAndTag_TagNameIn(post, Collections.singletonList("Java"));
        verify(postTagRepository, times(1)).deleteAllInBatch(Collections.singletonList(postTagJava));

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());
    }

    @Test
    @DisplayName("게시글 태그 업데이트 성공 - 태그 목록이 비어있는 경우")
    void updateTagsForPost_success_emptyTagNames() {
        // given
        List<String> newTags = Collections.emptyList();

        when(postTagRepository.findByPost(post)).thenReturn(Arrays.asList(
                createPostTag("Java", 1L),
                createPostTag("Spring", 2L)
        ));

        PostTag postTagJava = createPostTag("Java", 1L);
        PostTag postTagSpring = createPostTag("Spring", 2L);
        when(postTagRepository.findByPostAndTag_TagNameIn(post, Arrays.asList("Java", "Spring"))).thenReturn(Arrays.asList(postTagJava, postTagSpring));

        // when
        tagService.updateTagsForPost(post, newTags);

        // then
        verify(postTagRepository, times(1)).findByPost(post);
        verify(tagRepository, never()).findByTagNameIn(anyList());
        verify(tagRepository, never()).batchInsert(anyList());
        verify(postTagRepository, times(1)).findByPostAndTag_TagNameIn(post, Arrays.asList("Java", "Spring"));
        verify(postTagRepository, times(1)).deleteAllInBatch(Arrays.asList(postTagJava, postTagSpring));

        verify(cacheUtils, times(1)).evictTagListCache(post.getBlog().getId());
    }

    // Helper 메서드
    private Tag createTag(String tagName, Long id) {
        Tag tag = Tag.builder().tagName(tagName).build();
        TestUtil.setField(tag, "id", id);
        return tag;
    }

    private PostTag createPostTag(String tagName, Long id) {
        Tag tag = createTag(tagName, id);
        return PostTag.builder()
                .post(post)
                .tag(tag)
                .build();
    }
}