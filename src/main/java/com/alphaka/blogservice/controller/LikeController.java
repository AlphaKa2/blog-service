package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 게시글 좋아요 추가/취소
     */
    @PostMapping("post/{postId}")
    public ApiResponse<Void> toggleLikeOnPost(HttpServletRequest httpRequest,
                                              @PathVariable("postId") Long postId) {
        likeService.toggleLikeOnPost(httpRequest, postId);
        return new ApiResponse<>(null);
    }

    /**
     * 댓글 좋아요 추가/취소
     */
    @PostMapping("comment/{commentId}")
    public ApiResponse<Void> toggleLikeOnComment(HttpServletRequest httpRequest,
                                                 @PathVariable("commentId") Long commentId) {
        likeService.toggleLikeOnComment(httpRequest, commentId);
        return new ApiResponse<>(null);
    }
}
