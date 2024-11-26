package com.alphaka.blogservice.report.service;

import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.report.dto.ReportRequest;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.report.entity.Report;
import com.alphaka.blogservice.exception.custom.InvalidReportTargetException;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.post.repository.PostRepository;
import com.alphaka.blogservice.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final UserClient userClient;
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 사용자 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @Transactional
    public void reportUser(CurrentUser currentUser, ReportRequest request) {
        log.info("사용자 신고. 신고자: {}, 신고 대상: {}", currentUser.getUserId(), request.getTargetId());

        // 신고자 정보 확인
        UserDTO targetUser = userClient.findUserById(request.getTargetId()).getData();

        if (targetUser == null) {
            log.warn("신고 대상 사용자가 존재하지 않습니다.");
            throw new InvalidReportTargetException();
        }

        // 현재 사용자와 신고자 정보가 같은지 확인
        if (currentUser.getUserId().equals(request.getTargetId())) {
            log.warn("잘못된 신고 대상입니다.");
            throw new InvalidReportTargetException();
        }

        // 신고 생성
        Report report = Report.reportUser(
                currentUser.getUserId(),
                request.getTargetId(),
                request.getReason(),
                request.getDetails()
        );
        reportRepository.save(report);
    }

    /**
     * 게시글 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @Transactional
    public void reportPost(CurrentUser currentUser, ReportRequest request) {
        log.info("게시글 신고. 신고자: {}, 신고 대상: {}", currentUser.getUserId(), request.getTargetId());

        // 신고할 게시글 정보 확인
        Post post = postRepository.findById(request.getTargetId()).orElseThrow(InvalidReportTargetException::new);

        if (post == null) {
            log.warn("신고 대상 게시글이 존재하지 않습니다.");
            throw new InvalidReportTargetException();
        }

        // 현재 사용자와 게시글 작성자 정보가 같은지 확인
        if (currentUser.getUserId().equals(post.getUserId())) {
            log.warn("잘못된 신고 대상입니다.");
            throw new InvalidReportTargetException();
        }

        // 신고 생성
        Report report = Report.reportPost(
                currentUser.getUserId(),
                post,
                request.getReason(),
                request.getDetails()
        );
        reportRepository.save(report);
    }

    /**
     * 댓글 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @Transactional
    public void reportComment(CurrentUser currentUser, ReportRequest request) {
        log.info("댓글 신고. 신고자: {}, 신고 대상: {}", currentUser.getUserId(), request.getTargetId());

        // 신고 대상 확인
        Comment comment = commentRepository.findById(request.getTargetId()).orElseThrow(InvalidReportTargetException::new);

        if (comment == null) {
            log.warn("신고 대상 댓글이 존재하지 않습니다.");
            throw new InvalidReportTargetException();
        }

        // 현재 사용자와 댓글 작성자 정보가 같은지 확인
        if (currentUser.getUserId().equals(comment.getUserId())) {
            log.warn("잘못된 신고 대상입니다.");
            throw new InvalidReportTargetException();
        }

        // 신고 생성
        Report report = Report.reportComment(
                currentUser.getUserId(),
                comment,
                request.getReason(),
                request.getDetails()
        );
        reportRepository.save(report);
    }
}
