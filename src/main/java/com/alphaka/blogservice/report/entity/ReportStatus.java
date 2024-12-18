package com.alphaka.blogservice.report.entity;

public enum ReportStatus {
    PENDING, // 처리 대기 중
    IN_PROGRESS, // 처리 중
    COMPLETED, // 처리 완료
    REJECTED // 처리 거부
}
