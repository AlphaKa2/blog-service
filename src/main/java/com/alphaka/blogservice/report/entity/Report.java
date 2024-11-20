package com.alphaka.blogservice.report.entity;

import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId; // 신고자 ID

    @Column
    private Long reportedId; // 신고 대상 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post; // 신고된 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment; // 신고된 댓글

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason; // 신고 사유

    @Lob
    private String details; // 신고 상세 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // 신고 처리 상태

    // 사용자 신고
    public static Report reportUser(Long reporterId, Long reportedId, Reason reason, String details) {
        Report report = new Report();
        report.reporterId = reporterId;
        report.reportedId = reportedId;
        report.reason = reason;
        report.details = details;
        return report;
    }

    // 게시글 신고
    public static Report reportPost(Long reporterId, Post post, Reason reason, String details) {
        Report report = new Report();
        report.reporterId = reporterId;
        report.post = post;
        report.reason = reason;
        report.details = details;
        return report;
    }

    // 댓글 신고
    public static Report reportComment(Long reporterId, Comment comment, Reason reason, String details) {
        Report report = new Report();
        report.reporterId = reporterId;
        report.comment = comment;
        report.reason = reason;
        report.details = details;
        return report;
    }
}
