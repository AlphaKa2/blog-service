package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.ApiResponse;
import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.dto.request.CommentUpdateRequest;
import com.alphaka.blogservice.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ApiResponse<Void> createComment(HttpServletRequest httpRequest,
                                     @RequestBody CommentCreateRequest request) {
        commentService.createComment(httpRequest, request);
        return new ApiResponse<>(null);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ApiResponse<Void> updateComment(HttpServletRequest httpRequest,
                                     @PathVariable("commentId") Long commentId,
                                     @RequestBody CommentUpdateRequest request) {
        commentService.updateComment(httpRequest, commentId, request);
        return new ApiResponse<>(null);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(HttpServletRequest httpRequest,
                                     @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(httpRequest, commentId);
        return new ApiResponse<>(null);
    }
}
