package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.dto.request.CommentUpdateRequest;
import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.dto.response.CommentResponse;
import com.alphaka.blogservice.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ApiResponse<Long> createComment(HttpServletRequest httpRequest,
                                           @Valid @RequestBody CommentCreateRequest request) {
        Long response = commentService.createComment(httpRequest, request);
        return new ApiResponse<>(response);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ApiResponse<Long> updateComment(HttpServletRequest httpRequest,
                                           @PathVariable("commentId") Long commentId,
                                           @Valid @RequestBody CommentUpdateRequest request) {
        Long response = commentService.updateComment(httpRequest, commentId, request);
        return new ApiResponse<>(response);
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

    /**
     * 특정 게시글의 댓글 조회
     */
    @GetMapping("/post/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsForPost(HttpServletRequest httpRequest,
                                                                 @PathVariable("postId") Long postId) {
        List<CommentResponse> response = commentService.getCommentsForPost(httpRequest, postId);
        return new ApiResponse<>(response);
    }
}
