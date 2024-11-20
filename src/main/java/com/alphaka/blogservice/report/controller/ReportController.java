package com.alphaka.blogservice.report.controller;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.report.dto.ReportRequest;
import com.alphaka.blogservice.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 사용자 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @PostMapping("/users")
    public ApiResponse<Void> reportUser(CurrentUser currentUser,
                                        @Valid @RequestBody ReportRequest request) {
        reportService.reportUser(currentUser, request);
        return new ApiResponse<>(null);
    }

    /**
     * 게시글 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @PostMapping("/posts")
    public ApiResponse<Void> reportPost(CurrentUser currentUser,
                                        @Valid @RequestBody ReportRequest request) {
        reportService.reportPost(currentUser, request);
        return new ApiResponse<>(null);
    }

    /**
     * 댓글 신고하기
     * @param currentUser - 현재 사용자 정보
     * @param request - 신고 정보
     */
    @PostMapping("/comments")
    public ApiResponse<Void> reportComment(CurrentUser currentUser,
                                        @Valid @RequestBody ReportRequest request) {
        reportService.reportPost(currentUser, request);
        return new ApiResponse<>(null);
    }
}
