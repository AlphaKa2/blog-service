package com.alphaka.blogservice.comment.controller;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.comment.dto.CommentCreateRequest;
import com.alphaka.blogservice.comment.dto.CommentUpdateRequest;
import com.alphaka.blogservice.comment.dto.CommentResponse;
import com.alphaka.blogservice.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
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
    public ApiResponse<Long> createComment(CurrentUser currentUser,
                                           @Valid @RequestBody CommentCreateRequest request) {
        Long response = commentService.createComment(currentUser, request);
        return new ApiResponse<>(response);
    }

    /**
     * 댓글 수정을 위한 조회
     */
    @GetMapping("/{commentId}/edit")
    public ApiResponse<CommentUpdateRequest> getCommentUpdateData(CurrentUser currentUser,
                                                                  @PathVariable("commentId") Long commentId) {
        CommentUpdateRequest response = commentService.getCommentUpdateData(currentUser, commentId);
        return new ApiResponse<>(response);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ApiResponse<Long> updateComment(CurrentUser currentUser,
                                           @PathVariable("commentId") Long commentId,
                                           @Valid @RequestBody CommentUpdateRequest request) {
        Long response = commentService.updateComment(currentUser, commentId, request);
        return new ApiResponse<>(response);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(CurrentUser currentUser,
                                           @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(currentUser, commentId);
        return new ApiResponse<>(null);
    }

    /**
     * 특정 게시글의 댓글 조회
     */
    @GetMapping("/post/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsForPost(@Nullable CurrentUser currentUser,
                                                                 @PathVariable("postId") Long postId) {
        List<CommentResponse> response = commentService.getCommentsForPost(currentUser, postId);
        return new ApiResponse<>(response);
    }
}
