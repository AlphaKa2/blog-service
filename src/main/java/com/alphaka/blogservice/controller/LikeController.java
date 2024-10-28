package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.service.LikeService;
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
    public ApiResponse<Void> toggleLikeOnPost(CurrentUser currentUser,
                                              @PathVariable("postId") Long postId) {
        likeService.toggleLikeOnPost(currentUser, postId);
        return new ApiResponse<>(null);
    }

    /**
     * 댓글 좋아요 추가/취소
     */
    @PostMapping("comment/{commentId}")
    public ApiResponse<Void> toggleLikeOnComment(CurrentUser currentUser,
                                                 @PathVariable("commentId") Long commentId) {
        likeService.toggleLikeOnComment(currentUser, commentId);
        return new ApiResponse<>(null);
    }
}
