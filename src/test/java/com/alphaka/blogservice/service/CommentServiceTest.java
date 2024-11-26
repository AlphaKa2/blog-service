package com.alphaka.blogservice.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.comment.dto.CommentCreateRequest;
import com.alphaka.blogservice.comment.dto.CommentResponse;
import com.alphaka.blogservice.comment.dto.CommentUpdateRequest;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.comment.service.CommentService;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.exception.custom.*;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.post.repository.PostRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CacheUtils cacheUtils;

    @Mock
    private UserClient userClient;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private CurrentUser currentUser;
    private Blog blog;
    private Post post;

    @BeforeEach
    void setUp() {
        // 현재 사용자 생성
        currentUser = new CurrentUser(
                1L,
                "tester",
                "profileImage.png",
                "ROLE_USER"
        );

        // 블로그 생성
        blog = Blog.builder()
                .userId(1L)
                .build();
        TestUtil.setField(blog, "id", 1L);
        lenient().when(blogRepository.findByUserId(currentUser.getUserId())).thenReturn(Optional.of(blog));

        // 게시물 생성
        post = Post.builder()
                .userId(1L)
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(post, "id", 1L);
        lenient().when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    }

    @Test
    @DisplayName("댓글 작성 성공 - 부모 댓글 없이")
    void createComment_success_noParentComment() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                1L,
                "This is a test comment.",
                null,
                true
        );

        // 댓글 저장 객체 캡쳐
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment argument = invocation.getArgument(0);
            TestUtil.setField(argument, "id", 1L); // ID 설정
            return argument;
        });

        // when
        Long commentId = commentService.createComment(currentUser, request);

        // then
        assertThat(commentId).isEqualTo(1L);

        verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment capturedComment = commentCaptor.getValue();
        assertThat(capturedComment.getUserId()).isEqualTo(1L);
        assertThat(capturedComment.getPost()).isEqualTo(post);
        assertThat(capturedComment.getContent()).isEqualTo("This is a test comment.");
        assertThat(capturedComment.isPublic()).isTrue();
        assertThat(capturedComment.getParent()).isNull();

        verify(cacheUtils, times(1)).evictCommentsAndPostListCache(post.getBlog().getId(), post.getId());
    }

    @Test
    @DisplayName("댓글 작성 성공 - 부모 댓글과 함께")
    void createComment_success_withParentComment() {
        // given
        CommentCreateRequest requestParent = new CommentCreateRequest(
                1L,
                "This is a parent comment.",
                null,
                true
        );

        CommentCreateRequest requestChild = new CommentCreateRequest(
                1L,
                "This is a child comment.",
                1L,
                true
        );

        // 부모 댓글
        Comment parentComment = Comment.builder()
                .userId(1L)
                .post(post)
                .content("This is a parent comment.")
                .isPublic(true)
                .parent(null)
                .build();
        TestUtil.setField(parentComment, "id", 1L);

        // 자식 댓글
        Comment childComment = Comment.builder()
                .userId(1L)
                .post(post)
                .content("This is a child comment.")
                .isPublic(true)
                .parent(parentComment)
                .build();
        TestUtil.setField(childComment, "id", 2L);

        // Stubbing: 부모 댓글 저장
        doAnswer(invocation -> {
            Comment argument = invocation.getArgument(0);
            TestUtil.setField(argument, "id", 1L); // 부모 댓글 ID 설정
            return argument;
        }).when(commentRepository).save(argThat(comment -> comment.getParent() == null));

        // Stubbing: 자식 댓글 저장
        doAnswer(invocation -> {
            Comment argument = invocation.getArgument(0);
            TestUtil.setField(argument, "id", 2L); // 자식 댓글 ID 설정
            return argument;
        }).when(commentRepository).save(argThat(comment -> comment.getParent() != null));

        // 부모 댓글 검색 Stubbing
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));

        // ArgumentCaptor 설정
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Long parentCommentId = commentService.createComment(currentUser, requestParent);
        Long childCommentId = commentService.createComment(currentUser, requestChild);

        // then
        assertThat(parentCommentId).isEqualTo(1L);
        assertThat(childCommentId).isEqualTo(2L);

        // 모든 호출된 객체 검증
        verify(commentRepository, times(2)).save(commentCaptor.capture());
        List<Comment> capturedComments = commentCaptor.getAllValues();

        // 부모 댓글 검증
        Comment capturedParentComment = capturedComments.get(0);
        assertThat(capturedParentComment.getUserId()).isEqualTo(1L);
        assertThat(capturedParentComment.getPost()).isEqualTo(post);
        assertThat(capturedParentComment.getContent()).isEqualTo("This is a parent comment.");
        assertThat(capturedParentComment.isPublic()).isTrue();
        assertThat(capturedParentComment.getParent()).isNull();

        // 자식 댓글 검증
        Comment capturedChildComment = capturedComments.get(1);
        assertThat(capturedChildComment.getUserId()).isEqualTo(1L);
        assertThat(capturedChildComment.getPost()).isEqualTo(post);
        assertThat(capturedChildComment.getContent()).isEqualTo("This is a child comment.");
        assertThat(capturedChildComment.isPublic()).isTrue();
        assertThat(capturedChildComment.getParent()).isEqualTo(parentComment);

        verify(cacheUtils, times(2)).evictCommentsAndPostListCache(post.getBlog().getId(), post.getId());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 부모 댓글이 존재하지 않음")
    void createComment_fail_parentCommentNotFound() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                1L,
                "This is a test comment.",
                4444L, // 존재하지 않는 부모 댓글 ID
                true
        );

        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> commentService.createComment(currentUser, request))
                .isInstanceOf(ParentCommentNotFoundException.class);

        verify(commentRepository, times(1)).findById(request.getParentId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 부모 댓글이 다른 게시물에 속함")
    void createComment_fail_parentCommentBelongsToAnotherPost() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                1L,
                "This is a test comment.",
                2L,
                true
        );

        // 다른 게시글의 부모 댓글 생성
        Post anotherPost = Post.builder()
                .userId(1L)
                .title("Another Post")
                .content("This is another post.")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(anotherPost, "id", 2L);

        Comment parentComment = Comment.builder()
                .userId(1L)
                .post(anotherPost)
                .content("This is a parent comment in another post.")
                .isPublic(true)
                .build();
        TestUtil.setField(parentComment, "id", 2L);

        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(currentUser, request))
                .isInstanceOf(InvalidParentCommentException.class);

        verify(commentRepository, times(1)).findById(request.getParentId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 부모 댓글이 비공개 상태")
    public void createComment_fail_parentCommentIsPrivate() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                1L,
                "This is a test comment.",
                1L,
                true
        );

        // 부모 댓글 생성
        Comment parentComment = Comment.builder()
                .userId(1L)
                .post(post)
                .content("This is a parent comment.")
                .isPublic(false)
                .build();
        TestUtil.setField(parentComment, "id", 1L);

        when(commentRepository.findById(request.getParentId())).thenReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(currentUser, request))
                .isInstanceOf(InvalidParentCommentException.class);

        verify(commentRepository, times(1)).findById(request.getParentId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 게시글의 댓글 기능이 비활성화")
    void createComment_fail_postCommentableIsFalse() {
        // given

        // 댓글 기능 비활성화된 게시글
        Post noCommentPost = Post.builder()
                .userId(1L)
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(false)
                .build();
        TestUtil.setField(noCommentPost, "id", 2L);

        CommentCreateRequest request = new CommentCreateRequest(
                2L,
                "This is a test comment.",
                null,
                true
        );

        when(postRepository.findById(noCommentPost.getId())).thenReturn(Optional.of(noCommentPost));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(currentUser, request))
                .isInstanceOf(PrivateParentCommentException.class);

        verify(postRepository, times(1)).findById(noCommentPost.getId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 데이터 조회 성공")
    void getCommentUpdateData_success() {
        // given
        Comment comment = Comment.builder()
                .userId(1L)
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(postRepository.findByCommentsId(1L)).thenReturn(Optional.of(post));

        // when
        CommentUpdateRequest updateRequest = commentService.getCommentUpdateData(currentUser, 1L);

        // then
        assertThat(updateRequest.getContent()).isEqualTo("This is a test comment.");
        assertThat(updateRequest.isPublic()).isTrue();

        verify(commentRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).findByCommentsId(1L);
    }

    @Test
    @DisplayName("댓글 수정 데이터 조회 실패 - 댓글이 존재하지 않음")
    void getCommentUpdateData_fail_commentNotFound() {
        // given
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        when(postRepository.findByCommentsId(commentId)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> commentService.getCommentUpdateData(currentUser, commentId))
                .isInstanceOf(CommentNotFoundException.class);

        verify(commentRepository, times(1)).findById(commentId);
        verify(postRepository, times(1)).findByCommentsId(commentId);
    }

    @Test
    @DisplayName("댓글 수정 데이터 조회 실패 - 게시글을 찾을 수 없음")
    void getCommentUpdateData_fail_postNotFound() {
        // given
        Long commentId = 1L;

        when(postRepository.findByCommentsId(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentUpdateData(currentUser, commentId))
                .isInstanceOf(PostNotFoundException.class);

        verify(commentRepository, never()).findById(commentId);
        verify(postRepository, times(1)).findByCommentsId(commentId);
    }

    @Test
    @DisplayName("댓글 수정 데이터 조회 실패 - 게시글의 댓글 기능이 비활성화됨")
    void getCommentUpdateDate_fail_postIsNotCommentable() {
        // given
        Long commentId = 1L;

        Post noCommentPost = Post.builder()
                .userId(1L)
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(false)
                .build();
        TestUtil.setField(noCommentPost, "id", 2L);

        when(postRepository.findByCommentsId(commentId)).thenReturn(Optional.of(noCommentPost));

        // when & then
        assertThatThrownBy(() -> commentService.getCommentUpdateData(currentUser, commentId))
                .isInstanceOf(UnauthorizedException.class);

        verify(commentRepository, never()).findById(commentId);
        verify(postRepository, times(1)).findByCommentsId(commentId);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(
                "This is an updated comment.",
                false
        );

        Comment comment = Comment.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(postRepository.findByCommentsId(1L)).thenReturn(Optional.of(post));

        // when
        Long updatedCommentId = commentService.updateComment(currentUser, 1L, request);

        // then
        assertThat(updatedCommentId).isEqualTo(1L);
        assertThat(comment.getContent()).isEqualTo("This is an updated comment.");
        assertThat(comment.isPublic()).isFalse();

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(comment);
        verify(postRepository, times(1)).findByCommentsId(1L);
        verify(cacheUtils, times(1)).evictCommentsAndPostListCache(post.getBlog().getId(), post.getId());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글이 존재하지 않음")
    void updateComment_fail_commentNotFound() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(
                "This is an updated comment.",
                false
        );

        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(currentUser, 1L, request))
                .isInstanceOf(CommentNotFoundException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(postRepository, never()).findByCommentsId(1L);
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 게시글을 찾을 수 없음")
    void updateComment_fail_postNotFound() {
        // given
        Comment comment = Comment.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        CommentUpdateRequest request = new CommentUpdateRequest(
                "This is an updated comment.",
                false
        );

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(postRepository.findByCommentsId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(currentUser, 1L, request))
                .isInstanceOf(PostNotFoundException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).findByCommentsId(1L);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 게시글의 댓글 기능이 비활성화됨")
    void updateComment_fail_postIsNotCommentable() {
        // given
        Post noCommentPost = Post.builder()
                .userId(1L)
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(false)
                .build();

        Comment comment = Comment.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        CommentUpdateRequest request = new CommentUpdateRequest(
                "This is an updated comment.",
                false
        );

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(postRepository.findByCommentsId(1L)).thenReturn(Optional.of(noCommentPost));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(currentUser, 1L, request))
                .isInstanceOf(UnauthorizedException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(postRepository, times(1)).findByCommentsId(1L);
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 작성자가 아님")
    void updateComment_fail_userIsNotAuthor() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest(
                "This is an updated comment.",
                false
        );

        Comment comment = Comment.builder()
                .userId(2L) // 댓글 작성자 ID와 다름
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(currentUser, 1L, request))
                .isInstanceOf(UnauthorizedException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(postRepository, never()).findByCommentsId(1L);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        // given
        Comment comment = Comment.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(currentUser, 1L);

        // then
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).delete(comment);
        verify(cacheUtils, times(1)).evictCommentsAndPostListCache(post.getBlog().getId(), post.getId());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글이 존재하지 않음")
    void deleteComment_fail_commentNotFound() {
        // given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(currentUser, 1L))
                .isInstanceOf(CommentNotFoundException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 작성자가 아님")
    void deleteComment_fail_userIsNotAuthor() {
        // given
        Comment comment = Comment.builder()
                .userId(2L) // 댓글 작성자 ID와 다름
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(currentUser, 1L))
                .isInstanceOf(UnauthorizedException.class);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).delete(any(Comment.class));
        verify(cacheUtils, never()).evictCommentsAndPostListCache(anyLong(), anyLong());
    }

    @Test
    @DisplayName("특정 게시글의 댓글 조회 성공")
    void getCommentsForPost_success() {
        // given
        Long postId = post.getId();

        CommentResponse commentResponse1 = new CommentResponse(
                1L,
                null,
                currentUser.getUserId(),
                "First comment",
                10L,
                false,
                true,
                null,
                null
        );

        CommentResponse commentResponse2 = new CommentResponse(
                2L,
                1L,
                currentUser.getUserId(),
                "Reply to first comment",
                5L,
                false,
                true,
                null,
                null
        );

        List<CommentResponse> comments = Arrays.asList(commentResponse1, commentResponse2);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.getParentCommentResponse(postId, currentUser.getUserId())).thenReturn(comments);

        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                currentUser.getNickname(),
                currentUser.getProfileImage()
        );

        when(userClient.getUsersById(anySet())).thenReturn(new ApiResponse<>(Collections.singletonList(userDTO)));

        // when
        List<CommentResponse> responses = commentService.getCommentsForPost(currentUser, postId);

        // then
        assertThat(responses).hasSize(1);
        CommentResponse parentComment = responses.get(0);
        assertThat(parentComment.getCommentId()).isEqualTo(1L);
        assertThat(parentComment.getChildren()).hasSize(1);

        CommentResponse childComment = parentComment.getChildren().get(0);
        assertThat(childComment.getCommentId()).isEqualTo(2L);

        // Verify user details
        assertThat(parentComment.getAuthor()).isEqualTo(userDTO.getNickname());
        assertThat(childComment.getAuthor()).isEqualTo(userDTO.getNickname());

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).getParentCommentResponse(postId, currentUser.getUserId());
        verify(userClient, times(1)).getUsersById(anySet());
    }

    @Test
    @DisplayName("특정 게시글의 댓글 조회 실패 - 게시글이 존재하지 않음")
    void getCommentsForPost_fail_postNotFound() {
        // given
        Long postId = 999L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsForPost(currentUser, postId))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, never()).getParentCommentResponse(anyLong(), anyLong());
        verify(userClient, never()).getUsersById(anySet());
    }

    @Test
    @DisplayName("특정 게시글의 댓글 조회 성공 - 비공개 댓글 처리")
    void getCommentsForPost_success_privateComments() {
        // given
        Post newPost = Post.builder()
                .userId(3L) // 게시글 작성자를 현재 사용자와 다르게 설정
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(newPost, "id", 3L);

        Long postId = newPost.getId();

        CommentResponse privateComment = new CommentResponse(
                1L,
                null,
                2L, // 댓글 작성자를 현재 사용자와 다르게 설정
                "Private comment",
                5L,
                false,
                false, // isPublic = false
                null,
                null
        );

        List<CommentResponse> comments = Collections.singletonList(privateComment);

        when(postRepository.findById(postId)).thenReturn(Optional.of(newPost));
        when(commentRepository.getParentCommentResponse(postId, currentUser.getUserId())).thenReturn(comments);

        // when
        List<CommentResponse> responses = commentService.getCommentsForPost(currentUser, postId);

        // then
        assertThat(responses).hasSize(1);
        CommentResponse comment = responses.get(0);
        assertThat(comment.getAuthor()).isEqualTo("비공개 사용자"); // 비공개 처리 검증
        assertThat(comment.getContent()).isEqualTo("비공개 댓글입니다.");
        assertThat(comment.getLikeCount()).isEqualTo(0L);

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).getParentCommentResponse(postId, currentUser.getUserId());
    }

    @Test
    @DisplayName("특정 게시글의 댓글 조회 성공 - 비공개 댓글 작성자가 본인의 댓글을 볼 때")
    void getCommentsForPost_success_privateCommentByAuthor() {
        // given
        Long postId = post.getId();

        CommentResponse privateComment = new CommentResponse(
                1L,
                null,
                currentUser.getUserId(),
                "비공개 댓글",
                5L,
                false,
                false, // isPublic = false
                null,
                null
        );

        List<CommentResponse> comments = Collections.singletonList(privateComment);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.getParentCommentResponse(postId, currentUser.getUserId())).thenReturn(comments);

        UserDTO userDTO = new UserDTO(
                currentUser.getUserId(),
                currentUser.getNickname(),
                currentUser.getProfileImage()
        );

        when(userClient.getUsersById(anySet())).thenReturn(new ApiResponse<>(Collections.singletonList(userDTO)));

        // when
        List<CommentResponse> responses = commentService.getCommentsForPost(currentUser, postId);

        // then
        assertThat(responses).hasSize(1);
        CommentResponse comment = responses.get(0);
        assertThat(comment.getAuthor()).isEqualTo(userDTO.getNickname());
        assertThat(comment.getContent()).isEqualTo("비공개 댓글");
        assertThat(comment.getLikeCount()).isEqualTo(5L);

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).getParentCommentResponse(postId, currentUser.getUserId());
        verify(userClient, times(1)).getUsersById(anySet());
    }
}