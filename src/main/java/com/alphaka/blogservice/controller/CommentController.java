package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.dto.request.CommentUpdateRequest;
import com.alphaka.blogservice.dto.response.CommentDetailResponse;
import com.alphaka.blogservice.dto.response.CommentResponse;
import com.alphaka.blogservice.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResponse<CommentResponse> createComment(HttpServletRequest httpRequest,
                                                      @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(httpRequest, request);
        return new ApiResponse<>(response);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(HttpServletRequest httpRequest,
                                                      @PathVariable("commentId") Long commentId,
                                                      @RequestBody CommentUpdateRequest request) {
        CommentResponse response = commentService.updateComment(httpRequest, commentId, request);
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
    public ApiResponse<List<CommentDetailResponse>> getCommentsForPost(@PathVariable("postId") Long postId) {
        List<CommentDetailResponse> response = commentService.getCommentsForPost(postId);
        return new ApiResponse<>(response);
    }
}
