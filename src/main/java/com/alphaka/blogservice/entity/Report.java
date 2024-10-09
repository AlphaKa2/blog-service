package com.alphaka.blogservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long reporterId; // 신고자 ID

    @Column(nullable = false)
    private Long targetId; // 신고 대상 ID (Post, Comment, User)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType; // 신고 대상 타입

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // 신고 처리 상태

    @Column(nullable = false)
    private String reason;
}
