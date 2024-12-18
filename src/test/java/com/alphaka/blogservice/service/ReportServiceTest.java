package com.alphaka.blogservice.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.exception.custom.InvalidReportTargetException;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.post.repository.PostRepository;
import com.alphaka.blogservice.report.dto.ReportRequest;
import com.alphaka.blogservice.report.entity.Reason;
import com.alphaka.blogservice.report.entity.Report;
import com.alphaka.blogservice.report.repository.ReportRepository;
import com.alphaka.blogservice.report.service.ReportService;
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
class ReportServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private CurrentUser currentUser;
    private UserDTO targetUser;
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

        // 신고 대상 사용자 생성
        targetUser = new UserDTO(
                2L,
                "target",
                "profileImage.png"
        );

        // 블로그 생성
        blog = Blog.builder()
                .userId(2L)
                .build();
        TestUtil.setField(blog, "id", 2L);
        lenient().when(blogRepository.findByUserId(currentUser.getUserId())).thenReturn(Optional.of(blog));

        // 게시물 생성
        post = Post.builder()
                .userId(2L)
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
                .userId(2L)
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(comment, "id", 1L);
        lenient().when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
    }

    @Test
    @DisplayName("사용자 신고 성공")
    void reportUser_success() {
        // given
        ReportRequest request = new ReportRequest(
                targetUser.getUserId(),
                Reason.INAPPROPRIATE_NICKNAME,
                "부적절한 닉네임 신고"
        );

        when(userClient.findUserById(targetUser.getUserId())).thenReturn(new ApiResponse<>(targetUser));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);

        // when
        reportService.reportUser(currentUser, request);

        // then
        verify(userClient, times(1)).findUserById(targetUser.getUserId());
        verify(reportRepository, times(1)).save(captor.capture());

        Report report = captor.getValue();
        assertThat(report).isNotNull();
        assertThat(report.getReporterId()).isEqualTo(1L);
        assertThat(report.getReportedId()).isEqualTo(2L);
        assertThat(report.getReason()).isEqualTo(Reason.INAPPROPRIATE_NICKNAME);
        assertThat(report.getDetails()).isEqualTo("부적절한 닉네임 신고");
    }

    @Test
    @DisplayName("사용자 신고 실패 - 잘못된 신고 대상(자기자신 신고)")
    void reportUser_fail_selfReport() {
        // given
        ReportRequest request = new ReportRequest(
                currentUser.getUserId(),
                Reason.INAPPROPRIATE_NICKNAME,
                "부적절한 닉네임 신고"
        );

        UserDTO currentUserDTO = new UserDTO(
                currentUser.getUserId(),
                currentUser.getNickname(),
                currentUser.getProfileImage()
        );

        when(userClient.findUserById(currentUser.getUserId())).thenReturn(new ApiResponse<>(currentUserDTO));

        // when & then
        assertThatThrownBy(() -> reportService.reportUser(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(userClient, times(1)).findUserById(currentUser.getUserId());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자 신고 실패 - 잘못된 신고 대상(존재하지 않는 사용자)")
    void reportUser_fail_userNotFound() {
        // given
        ReportRequest request = new ReportRequest(
                999L,
                Reason.INAPPROPRIATE_NICKNAME,
                "부적절한 닉네임 신고"
        );

        when(userClient.findUserById(999L)).thenReturn(new ApiResponse<>(null));

        // when & then
        assertThatThrownBy(() -> reportService.reportUser(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(userClient, times(1)).findUserById(999L);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 신고 성공")
    void reportPost_success() {
        // given
        ReportRequest request = new ReportRequest(
                post.getId(),
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 게시글 신고"
        );

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);

        // when
        reportService.reportPost(currentUser, request);

        // then
        verify(postRepository, times(1)).findById(post.getId());
        verify(reportRepository, times(1)).save(captor.capture());

        Report report = captor.getValue();
        assertThat(report).isNotNull();
        assertThat(report.getReporterId()).isEqualTo(currentUser.getUserId());
        assertThat(report.getPost()).isEqualTo(post);
        assertThat(report.getReason()).isEqualTo(Reason.SPAM_ADVERTISEMENT);
        assertThat(report.getDetails()).isEqualTo("스팸홍보 게시글 신고");
    }

    @Test
    @DisplayName("게시글 신고 실패 - 잘못된 신고 대상(자기자신 게시글 신고)")
    void reportPost_fail_selfPostReport() {
        // given
        Post myPost = Post.builder()
                .userId(currentUser.getUserId())
                .blog(blog)
                .title("This is a test post.")
                .content("<p>This is a test post content.<p>")
                .isPublic(true)
                .isCommentable(true)
                .build();
        TestUtil.setField(myPost, "id", 2L);

        ReportRequest request = new ReportRequest(
                myPost.getId(),
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 게시글 신고"
        );

        when(postRepository.findById(myPost.getId())).thenReturn(Optional.of(myPost));

        // when & then
        assertThatThrownBy(() -> reportService.reportPost(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(postRepository, times(1)).findById(myPost.getId());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 신고 실패 - 잘못된 신고 대상(존재하지 않는 게시글)")
    void reportPost_fail_postNotFound() {
        // given
        ReportRequest request = new ReportRequest(
                999L,
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 게시글 신고"
        );

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.reportPost(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(postRepository, times(1)).findById(999L);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 신고 성공")
    void reportComment_success() {
        // given
        ReportRequest request = new ReportRequest(
                comment.getId(),
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 댓글 신고"
        );

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);

        // when
        reportService.reportComment(currentUser, request);

        // then
        verify(commentRepository, times(1)).findById(comment.getId());
        verify(reportRepository, times(1)).save(captor.capture());

        Report report = captor.getValue();
        assertThat(report).isNotNull();
        assertThat(report.getReporterId()).isEqualTo(currentUser.getUserId());
        assertThat(report.getComment()).isEqualTo(comment);
        assertThat(report.getReason()).isEqualTo(Reason.SPAM_ADVERTISEMENT);
        assertThat(report.getDetails()).isEqualTo("스팸홍보 댓글 신고");
    }

    @Test
    @DisplayName("댓글 신고 실패 - 잘못된 신고 대상(자기자신 댓글 신고)")
    void reportComment_fail_selfCommentReport() {
        // given
        Comment myComment = Comment.builder()
                .userId(currentUser.getUserId())
                .post(post)
                .content("This is a test comment.")
                .isPublic(true)
                .build();
        TestUtil.setField(myComment, "id", 2L);

        ReportRequest request = new ReportRequest(
                myComment.getId(),
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 댓글 신고"
        );

        when(commentRepository.findById(myComment.getId())).thenReturn(Optional.of(myComment));

        // when & then
        assertThatThrownBy(() -> reportService.reportComment(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(commentRepository, times(1)).findById(myComment.getId());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 신고 실패 - 잘못된 신고 대상(존재하지 않는 댓글)")
    void reportComment_fail_commentNotFound() {
        // given
        ReportRequest request = new ReportRequest(
                999L,
                Reason.SPAM_ADVERTISEMENT,
                "스팸홍보 댓글 신고"
        );

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.reportComment(currentUser, request))
                .isInstanceOf(InvalidReportTargetException.class);

        verify(commentRepository, times(1)).findById(999L);
        verify(reportRepository, never()).save(any());
    }
}