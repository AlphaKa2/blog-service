package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.ApiResponse;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
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
    @PostMapping()
    public ApiResponse<PostResponse> createPost(HttpServletRequest httpRequest,
                                                @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(httpRequest, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(HttpServletRequest httpRequest,
                                                @PathVariable Long postId,
                                                @RequestBody PostUpdateRequest request) {
        PostResponse response = postService.updatePost(httpRequest, postId, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(HttpServletRequest request,
                                        @PathVariable Long postId) {
        postService.deletePost(request, postId);
        return new ApiResponse<>(null);
    }
}