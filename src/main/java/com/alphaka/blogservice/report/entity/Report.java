package com.alphaka.blogservice.report.entity;

import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.common.entity.CreateBaseEntity;
import com.alphaka.blogservice.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends CreateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;

    @Column
    private Long reportedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    @Lob
    private String details;

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
