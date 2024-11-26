package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.post.dto.PostRequest;
import com.alphaka.blogservice.post.dto.PostListResponse;
import com.alphaka.blogservice.post.dto.PostResponse;
import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.post.service.PostService;
import com.alphaka.blogservice.post.repository.PostRepository;
import com.alphaka.blogservice.tag.service.TagService;
import com.alphaka.blogservice.util.CacheUtils;
import com.alphaka.blogservice.util.TestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private CacheUtils cacheUtils;

    @Mock
    private TagService tagService;

    @Mock
    private UserClient userClient;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PostService postService;

    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new CurrentUser(
                1L,
                "tester",
                "profileImage.png",
                "ROLE_USER"
        );
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() {
        // given
        PostRequest request = new PostRequest(
                "Test Post",
                "<p>This is a test post.</p>",
                true,
                true,
                Arrays.asList("Spring", "Java")
        );

        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        when(blogRepository.findByUserId(currentUser.getUserId())).thenReturn(Optional.of(blog));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            TestUtil.setField(post, "id", 1L);
            return post;
        });

        // when
        Long postId = postService.createPost(currentUser, request);

        // then
        assertThat(postId).isNotNull().isEqualTo(1L);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class); // postRepository에 전달된 Post 객체를 캡쳐하여 검증
        verify(postRepository, times(1)).save(postCaptor.capture());

        Post capturedPost = postCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo("Test Post"); // 캡쳐된 post 객체의 필드 값 검증
        assertThat(capturedPost.getContent()).isEqualTo("<p>This is a test post.</p>");
        assertThat(capturedPost.isPublic()).isTrue();
        assertThat(capturedPost.isCommentable()).isTrue();
        assertThat(capturedPost.getBlog()).isEqualTo(blog);
        assertThat(capturedPost.getUserId()).isEqualTo(currentUser.getUserId());

        verify(tagService, times(1)).addTagsToPost(capturedPost, request.getTagNames()); // 다른 서비스 호출 검증
        verify(cacheUtils, times(1)).evictPostListAndTagListCache(blog.getId());
    }

    @Test
    @DisplayName("태그 없이 게시글 생성 성공")
    void createPost_success_noTags() {
        // given
        PostRequest request = new PostRequest(
                "Test Post",
                "<p>This is a test post.</p>",
                true,
                true,
                Collections.emptyList() // 태그가 없는 경우
        );

        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L); // 리플렉션으로 ID 설정

        when(blogRepository.findByUserId(currentUser.getUserId())).thenReturn(Optional.of(blog));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            TestUtil.setField(post, "id", 1L); // 리플렉션으로 ID 설정
            return post;
        });

        // when
        Long postId = postService.createPost(currentUser, request);

        // then
        assertThat(postId).isNotNull().isEqualTo(1L);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class); // postRepository에 전달된 Post 객체를 캡쳐하여 검증
        verify(postRepository, times(1)).save(postCaptor.capture());

        Post capturedPost = postCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo("Test Post"); // 저장된 Post의 필드 값 검증
        assertThat(capturedPost.getContent()).isEqualTo("<p>This is a test post.</p>");
        assertThat(capturedPost.isPublic()).isTrue();
        assertThat(capturedPost.isCommentable()).isTrue();
        assertThat(capturedPost.getPostTags()).isNull();
        assertThat(capturedPost.getBlog()).isEqualTo(blog);

        verify(tagService, never()).addTagsToPost(any(Post.class), anyList()); // 태그 서비스가 호출되지 않았는지 확인
        verify(cacheUtils, times(1)).evictPostListAndTagListCache(blog.getId()); // 캐시 무효화 호출 검증
    }

    @Test
    @DisplayName("게시글 생성 실패 - 블로그를 찾을 수 없음")
    void createPost_fail_blogNotFound() {
        // given
        PostRequest request = new PostRequest(
                "Test Post",
                "<p>This is a test post.</p>",
                true,
                true,
                Collections.emptyList()
        );

        when(blogRepository.findByUserId(currentUser.getUserId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(currentUser, request))
                .isInstanceOf(BlogNotFoundException.class);

        verify(blogRepository, times(1)).findByUserId(currentUser.getUserId());
        verify(postRepository, never()).save(any(Post.class));
        verify(tagService, never()).addTagsToPost(any(Post.class), anyList());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 데이터 조회 성공")
    void getPostUpdateData_success() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .title("Original Title")
                .content("Original Content")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(tagService.findTagsByPostId(postId)).thenReturn(Arrays.asList("Spring", "Java"));

        // when
        PostRequest updateRequest = postService.getPostUpdateData(currentUser, postId);

        // then
        assertThat(updateRequest.getTitle()).isEqualTo("Original Title");
        assertThat(updateRequest.getContent()).isEqualTo("Original Content");
        assertThat(updateRequest.isPublic()).isTrue();
        assertThat(updateRequest.isCommentable()).isTrue();
        assertThat(updateRequest.getTagNames()).containsExactly("Spring", "Java");

        verify(postRepository, times(1)).findById(postId);
        verify(tagService, times(1)).findTagsByPostId(postId);
    }

    @Test
    @DisplayName("게시글 수정 데이터 조회 실패 - 게시글을 찾을 수 없음")
    void getPostUpdateData_fail_postNotFound() {
        // given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostUpdateData(currentUser, postId))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(tagService, never()).findTagsByPostId(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 데이터 조회 실패 - 권한 없음")
    void getPostUpdateData_fail_unauthorized() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .userId(2L) // 다른 사용자 ID
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.getPostUpdateData(currentUser, postId))
                .isInstanceOf(UnauthorizedException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(tagService, never()).findTagsByPostId(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        // given
        Long postId = 1L;
        PostRequest request = PostRequest.builder()
                .title("Updated Title")
                .content("<p>Updated Content</p>")
                .isPublic(false)
                .isCommentable(false)
                .tagNames(Arrays.asList("Spring Boot", "Testing"))
                .build();

        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .blog(blog)
                .title("Original Title")
                .content("<p>Original Content</p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        // when
        Long updatedPostId = postService.updatePost(currentUser, postId, request);

        // then
        assertThat(updatedPostId).isEqualTo(postId);
        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getContent()).isEqualTo("<p>Updated Content</p>");
        assertThat(post.isPublic()).isFalse();
        assertThat(post.isCommentable()).isFalse();

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).save(post);
        verify(tagService, times(1)).updateTagsForPost(post, request.getTagNames());
        verify(cacheUtils, times(1)).evictPostListAndTagListCache(blog.getId());
        verify(cacheUtils, times(1)).evictPostDetailsCache(postId);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글을 찾을 수 없음")
    void updatePost_fail_postNotFound() {
        // given
        Long postId = 1L;
        PostRequest request = PostRequest.builder()
                .title("Updated Title")
                .content("<p>Updated Content</p>")
                .isPublic(false)
                .isCommentable(false)
                .tagNames(Arrays.asList("Spring Boot", "Testing"))
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(currentUser, postId, request))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
        verify(tagService, never()).updateTagsForPost(any(Post.class), anyList());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
        verify(cacheUtils, never()).evictPostDetailsCache(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePost_fail_unauthorized() {
        // given
        Long postId = 1L;
        PostRequest request = PostRequest.builder()
                .title("Updated Title")
                .content("<p>Updated Content</p>")
                .isPublic(false)
                .isCommentable(false)
                .tagNames(Arrays.asList("Spring Boot", "Testing"))
                .build();

        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        Post post = Post.builder()
                .userId(2L) // 다른 사용자 ID
                .blog(blog)
                .title("Original Title")
                .content("<p>Original Content</p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(currentUser, postId, request))
                .isInstanceOf(UnauthorizedException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
        verify(tagService, never()).updateTagsForPost(any(Post.class), anyList());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
        verify(cacheUtils, never()).evictPostDetailsCache(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 태그 서비스 예외 발생")
    void updatePost_fail_tagServiceException() {
        // given
        Long postId = 1L;
        PostRequest request = PostRequest.builder()
                .title("Updated Title")
                .content("<p>Updated Content</p>")
                .isPublic(false)
                .isCommentable(false)
                .tagNames(Arrays.asList("Spring Boot", "Testing"))
                .build();

        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .blog(blog)
                .title("Original Title")
                .content("<p>Original Content</p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        doThrow(new RuntimeException("Tag Service Error")).when(tagService).updateTagsForPost(post, request.getTagNames());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(currentUser, postId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tag Service Error");

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).save(post);
        verify(tagService, times(1)).updateTagsForPost(post, request.getTagNames());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
        verify(cacheUtils, never()).evictPostDetailsCache(anyLong());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        // given
        Long postId = 1L;
        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .blog(blog)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        // when
        postService.deletePost(currentUser, postId);

        // then
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(post);
        verify(cacheUtils, times(1)).evictCommentsCache(postId);
        verify(cacheUtils, times(1)).evictPostListAndTagListCache(blog.getId());
        verify(cacheUtils, times(1)).evictPostDetailsCache(postId);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글을 찾을 수 없음")
    void deletePost_fail_postNotFound() {
        // given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(currentUser, postId))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).delete(any(Post.class));
        verify(cacheUtils, never()).evictCommentsCache(anyLong());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
        verify(cacheUtils, never()).evictPostDetailsCache(anyLong());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_fail_unauthorized() {
        // given
        Long postId = 1L;
        Blog blog = Blog.builder()
                .userId(currentUser.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        Post post = Post.builder()
                .userId(2L) // 다른 사용자 ID
                .blog(blog)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(currentUser, postId))
                .isInstanceOf(UnauthorizedException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).delete(any(Post.class));
        verify(cacheUtils, never()).evictCommentsCache(anyLong());
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
        verify(cacheUtils, never()).evictPostDetailsCache(anyLong());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 - 공개 게시글")
    void getPostResponse_success_publicPost() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .title("Test Post")
                .content("<p>Test Content</p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", postId);

        PostResponse postResponse = new PostResponse(
                postId,
                currentUser.getUserId(),
                "Test Post",
                "<p>Test Content</p>",
                10L,
                100,
                false,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                currentUser.getNickname(),
                currentUser.getProfileImage()
        );

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.getPostResponse(postId, currentUser.getUserId())).thenReturn(Optional.of(postResponse));
        when(userClient.findUserById(currentUser.getUserId())).thenReturn(new ApiResponse<>(userDTO));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // Redis 모킹
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        when(tagService.findTagsByPostId(postId)).thenReturn(Arrays.asList("Spring", "Java"));

        // when
        PostResponse response = postService.getPostResponse(request, currentUser, postId);

        // then
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getAuthor()).isEqualTo("tester");
        assertThat(response.getTags()).containsExactly("Spring", "Java");
        assertThat(response.getViewCount()).isEqualTo(100);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).getPostResponse(postId, currentUser.getUserId());
        verify(userClient, times(1)).findUserById(currentUser.getUserId());
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(postRepository, times(1)).increaseViewCount(postId);
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 게시글을 찾을 수 없음")
    void getPostResponse_fail_postNotFound() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostResponse(request, currentUser, postId))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).getPostResponse(anyLong(), anyLong());
        verify(userClient, never()).findUserById(anyLong());
        verify(redisTemplate, never()).opsForValue();
        verify(postRepository, never()).increaseViewCount(anyLong());
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 비공개 게시글, 권한 없음")
    void getPostResponse_fail_privatePost_unauthorized() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);

        Post post = Post.builder()
                .userId(2L) // 다른 사용자 ID
                .isPublic(false)
                .build();
        TestUtil.setField(post, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.getPostResponse(request, currentUser, postId))
                .isInstanceOf(UnauthorizedException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).getPostResponse(anyLong(), anyLong());
        verify(userClient, never()).findUserById(anyLong());
        verify(redisTemplate, never()).opsForValue();
        verify(postRepository, never()).increaseViewCount(anyLong());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 - 단일 게시글 조회")
    void getPostListResponse_success_singlePost() {
        // given
        String nickname = "tester";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                nickname,
                currentUser.getProfileImage()
        );

        Blog blog = Blog.builder()
                .userId(userDTO.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.of(blog));

        PostListResponse postListResponse = new PostListResponse( // 썸네일과 태그는 서비스에서 설정
                1L,
                "Test Post",
                "This is a test post content snippet...",
                5L,
                2L,
                50,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(postRepository.getPostListResponse(blog.getId(), true, pageable))
                .thenReturn(Collections.singletonList(postListResponse));

        when(tagService.findTagsByPostIds(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonMap(1L, Arrays.asList("Spring", "Java")));

        // when
        List<PostListResponse> responses = postService.getPostListResponse(currentUser, nickname, pageable);

        // then
        assertThat(responses).hasSize(1);
        PostListResponse response = responses.get(0);
        assertThat(response.getPostId()).isEqualTo(1L);
        assertThat(response.getTags()).containsExactly("Spring", "Java");
        assertThat(response.getRepresentativeImage()).isNull(); // 썸네일이 설정되지 않은 경우
        assertThat(response.getContentSnippet()).isEqualTo("This is a test post content snippet...");

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
        verify(postRepository, times(1)).getPostListResponse(blog.getId(), true, pageable);
        verify(tagService, times(1)).findTagsByPostIds(Collections.singletonList(1L));
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 - 여러 게시글 조회")
    void getPostListResponse_success_multiplePosts() {
        // given
        String nickname = "tester";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                nickname,
                currentUser.getProfileImage()
        );

        Blog blog = Blog.builder()
                .userId(userDTO.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.of(blog));

        List<PostListResponse> postList = Arrays.asList(
                new PostListResponse(
                        1L,
                        "Test Post 1",
                        "This is a test post content snippet 1...",
                        5L,
                        2L,
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                new PostListResponse(
                        2L,
                        "Test Post 2",
                        "This is a test post content snippet 2...",
                        3L,
                        1L,
                        30,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        when(postRepository.getPostListResponse(blog.getId(), true, pageable)).thenReturn(postList);

        when(tagService.findTagsByPostIds(Arrays.asList(1L, 2L))).thenReturn(
                Map.of(
                        1L, Arrays.asList("Spring", "Java"),
                        2L, Arrays.asList("Microservices", "Docker")
                )
        );

        // when
        List<PostListResponse> responses = postService.getPostListResponse(currentUser, nickname, pageable);

        // then
        assertThat(responses).hasSize(2);

        PostListResponse response1 = responses.get(0);
        assertThat(response1.getPostId()).isEqualTo(1L);
        assertThat(response1.getTags()).containsExactly("Spring", "Java");
        assertThat(response1.getContentSnippet()).isEqualTo("This is a test post content snippet 1...");

        PostListResponse response2 = responses.get(1);
        assertThat(response2.getPostId()).isEqualTo(2L);
        assertThat(response2.getTags()).containsExactly("Microservices", "Docker");
        assertThat(response2.getContentSnippet()).isEqualTo("This is a test post content snippet 2...");

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
        verify(postRepository, times(1)).getPostListResponse(blog.getId(), true, pageable);
        verify(tagService, times(1)).findTagsByPostIds(Arrays.asList(1L, 2L));
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글 목록 조회 실패 - 블로그를 찾을 수 없음")
    void getPostListResponse_fail_blogNotFound() {
        // given
        String nickname = "nonexistentuser";
        Pageable pageable = mock(Pageable.class);
        UserDTO userDTO = new UserDTO(
                2L,
                nickname,
                "anotherProfile.png"
        );

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.empty());

        // when & Then
        assertThatThrownBy(() -> postService.getPostListResponse(currentUser, nickname, pageable))
                .isInstanceOf(BlogNotFoundException.class);

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
        verify(postRepository, never()).getPostListResponse(anyLong(), anyBoolean(), any(Pageable.class));
        verify(tagService, never()).findTagsByPostIds(anyList());
    }

    @Test
    @DisplayName("게시글 목록 조회 시 비소유자일 경우 공개 게시글만 조회")
    void getPostListResponse_success_publicPostsOnlyForNotOwner() {
        // given
        String nickname = "otheruser";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        UserDTO userDTO = new UserDTO(
                2L,
                nickname,
                "otherProfile.png"
        );

        Blog blog = Blog.builder()
                .userId(userDTO.getUserId())
                .build();
        TestUtil.setField(blog, "id", 2L);

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.of(blog));

        PostListResponse postListResponse = new PostListResponse( // 썸네일과 태그는 서비스에서 설정
                2L,
                "Another Test Post",
                "Another test post content snippet...",
                3L,
                1L,
                30,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(postRepository.getPostListResponse(blog.getId(), false, pageable))
                .thenReturn(Collections.singletonList(postListResponse));

        when(tagService.findTagsByPostIds(Collections.singletonList(2L)))
                .thenReturn(Collections.singletonMap(2L, Arrays.asList("Microservices", "Docker")));

        // when
        List<PostListResponse> responses = postService.getPostListResponse(currentUser, nickname, pageable);

        // then
        assertThat(responses).hasSize(1);
        PostListResponse response = responses.get(0);
        assertThat(response.getPostId()).isEqualTo(2L);
        assertThat(response.getTags()).containsExactly("Microservices", "Docker");
        assertThat(response.getRepresentativeImage()).isNull();
        assertThat(response.getContentSnippet()).isEqualTo("Another test post content snippet...");

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
        verify(postRepository, times(1)).getPostListResponse(blog.getId(), false, pageable);
        verify(tagService, times(1)).findTagsByPostIds(Collections.singletonList(2L));
        verify(cacheUtils, never()).evictPostListAndTagListCache(anyLong());
    }

    @Test
    @DisplayName("게시글 조회수 증가 성공 - 새로운 IP")
    void increaseViewCount_success_newView() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ipAddress = "127.0.0.1";
        String redisKey = "post:viewCount:" + postId + ":" + ipAddress;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS)).thenReturn(true);

        // when
        postService.increaseViewCount(postId, request);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS);
        verify(postRepository, times(1)).increaseViewCount(postId);
    }

    @Test
    @DisplayName("게시글 조회수 증가 실패 - 이미 조회한 IP")
    void increaseViewCount_fail_alreadyViewed() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ipAddress = "127.0.0.1";
        String redisKey = "post:viewCount:" + postId + ":" + ipAddress;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS)).thenReturn(false);

        // when
        postService.increaseViewCount(postId, request);

        // then
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS);
        verify(postRepository, never()).increaseViewCount(anyLong());
    }

    @Test
    @DisplayName("조회수 증가 실패 - Redis 예외 발생")
    void increaseViewCount_fail_redisException() {
        // given
        Long postId = 1L;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ipAddress = "127.0.0.1";
        String redisKey = "post:viewCount:" + postId + ":" + ipAddress;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS))
                .thenThrow(new RuntimeException("Redis Error"));

        // when & then
        assertThatThrownBy(() -> postService.increaseViewCount(postId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis Error");

        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).setIfAbsent(redisKey, "1", 1L, TimeUnit.DAYS);
        verify(postRepository, never()).increaseViewCount(anyLong());
    }

    @Test
    @DisplayName("닉네임으로 블로그 ID 조회 성공")
    void getBlogIdByNickname_success() {
        // given
        String nickname = "tester";
        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                nickname,
                currentUser.getProfileImage()
        );

        Blog blog = Blog.builder()
                .userId(userDTO.getUserId())
                .build();
        TestUtil.setField(blog, "id", 1L);

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.of(blog));

        // when
        Long blogId = postService.getBlogIdByNickname(nickname);

        // then
        assertThat(blogId).isEqualTo(1L);
        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
    }

    @Test
    @DisplayName("닉네임으로 블로그 ID 조회 실패 - 블로그를 찾을 수 없음")
    void getBlogIdByNickname_fail_blogNotFound() {
        // given
        String nickname = "nonexistentuser";
        UserDTO userDTO = new UserDTO(
                2L,
                nickname,
                "anotherProfile.png"
        );

        when(userClient.findUserByNickname(nickname)).thenReturn(new ApiResponse<>(userDTO));
        when(blogRepository.findByUserId(userDTO.getUserId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getBlogIdByNickname(nickname))
                .isInstanceOf(BlogNotFoundException.class);

        verify(userClient, times(1)).findUserByNickname(nickname);
        verify(blogRepository, times(1)).findByUserId(userDTO.getUserId());
    }
}