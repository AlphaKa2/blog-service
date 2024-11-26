package com.alphaka.blogservice.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.exception.custom.CommentNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.like.entity.Like;
import com.alphaka.blogservice.like.repository.LikeRepository;
import com.alphaka.blogservice.like.service.LikeService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private CacheUtils cacheUtils;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    private CurrentUser currentUser;
    private Blog blog;
    private Post post;
    private Comment comment;

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

        // 댓글 생성
        comment = Comment.builder()
                .userId(1L)
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);
        lenient().when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
    }

    @Test
    @DisplayName("게시글 좋아요 성공 - 처음 좋아요 누름")
    void toggleLikeOnPost_success_like() {
        // given
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.findByUserIdAndPost(currentUser.getUserId(), post)).thenReturn(Optional.empty());

        ArgumentCaptor<Like> likeCaptor = ArgumentCaptor.forClass(Like.class);

        // when
        likeService.toggleLikeOnPost(currentUser, post.getId());

        // then
        verify(likeRepository, times(1)).save(likeCaptor.capture());

        Like like = likeCaptor.getValue();

        assertThat(like.getUserId()).isEqualTo(currentUser.getUserId());
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.getComment()).isNull();

        verify(cacheUtils, times(1)).evictLikeCountForPost(post.getId());
        verify(cacheUtils, times(1)).evictUserLikeOnPost(currentUser.getUserId(), post.getId());
    }

    @Test
    @DisplayName("게시글 좋아요 취소 성공 - 이미 좋아요 누름")
    void toggleLikeOnPost_success_unlike() {
        // given
        Like existingLike = Like.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .build();
        TestUtil.setField(existingLike, "id", 1L);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.findByUserIdAndPost(currentUser.getUserId(), post)).thenReturn(Optional.of(existingLike));

        // when
        likeService.toggleLikeOnPost(currentUser, post.getId());

        // then
        verify(likeRepository, times(1)).delete(existingLike);

        verify(cacheUtils, times(1)).evictLikeCountForPost(post.getId());
        verify(cacheUtils, times(1)).evictUserLikeOnPost(currentUser.getUserId(), post.getId());
    }

    @Test
    @DisplayName("게시글 좋아요 실패 - 게시글 없음")
    void toggleLikeOnPost_fail_postNotFound() {
        // given
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.toggleLikeOnPost(currentUser, post.getId()))
                .isInstanceOf(PostNotFoundException.class);

        verify(likeRepository, never()).findByUserIdAndPost(anyLong(), any(Post.class));
        verify(likeRepository, never()).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));

        verify(cacheUtils, never()).evictLikeCountForPost(anyLong());
        verify(cacheUtils, never()).evictUserLikeOnPost(anyLong(), anyLong());
    }

    @Test
    @DisplayName("댓글 좋아요 성공 - 처음 좋아요 누름")
    void toggleLikeOnComment_success_like() {
        // given
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(likeRepository.findByUserIdAndComment(currentUser.getUserId(), comment)).thenReturn(Optional.empty());

        ArgumentCaptor<Like> likeCaptor = ArgumentCaptor.forClass(Like.class);

        // when
        likeService.toggleLikeOnComment(currentUser, comment.getId());

        // then
        verify(likeRepository, times(1)).save(likeCaptor.capture());

        Like like = likeCaptor.getValue();

        assertThat(like.getUserId()).isEqualTo(currentUser.getUserId());
        assertThat(like.getPost()).isNull();
        assertThat(like.getComment()).isEqualTo(comment);

        verify(cacheUtils, times(1)).evictLikeCountForComment(comment.getId());
        verify(cacheUtils, times(1)).evictUserLikeOnComment(currentUser.getUserId(), comment.getId());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공 - 이미 좋아요 누름")
    void toggleLikeOnComment_success_unlike() {
        // given
        Like existingLike = Like.builder()
                .userId(currentUser.getUserId())
                .comment(comment)
                .build();
        TestUtil.setField(existingLike, "id", 1L);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(likeRepository.findByUserIdAndComment(currentUser.getUserId(), comment)).thenReturn(Optional.of(existingLike));

        // when
        likeService.toggleLikeOnComment(currentUser, comment.getId());

        // then
        verify(likeRepository, times(1)).delete(existingLike);

        verify(cacheUtils, times(1)).evictLikeCountForComment(comment.getId());
        verify(cacheUtils, times(1)).evictUserLikeOnComment(currentUser.getUserId(), comment.getId());
    }

    @Test
    @DisplayName("댓글 좋아요 실패 - 댓글 없음")
    void toggleLikeOnComment_fail_commentNotFound() {
        // given
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.toggleLikeOnComment(currentUser, comment.getId()))
                .isInstanceOf(CommentNotFoundException.class);

        verify(likeRepository, never()).findByUserIdAndComment(anyLong(), any(Comment.class));
        verify(likeRepository, never()).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));

        verify(cacheUtils, never()).evictLikeCountForComment(anyLong());
        verify(cacheUtils, never()).evictUserLikeOnComment(anyLong(), anyLong());
    }
}