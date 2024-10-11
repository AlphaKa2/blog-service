package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.ApiResponse;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping("/create")
    public ApiResponse<PostResponse> createPost(@RequestHeader("Authorization") String token,
                                                @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(token, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/update/{postId}")
    public ApiResponse<PostResponse> updatePost(@RequestHeader("Authorization") String token,
                                                @PathVariable Long postId,
                                                @RequestBody PostUpdateRequest request) {
        PostResponse response = postService.updatePost(token, postId, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/delete/{postId}")
    public ApiResponse<Void> deletePost(@RequestHeader("Authorization") String token,
                                        @PathVariable Long postId) {
        postService.deletePost(token, postId);
        return new ApiResponse<>(null);
    }
}