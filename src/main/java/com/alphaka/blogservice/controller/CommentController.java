package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.ApiResponse;
import com.alphaka.blogservice.dto.request.CommentCreateRequest;
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
}
