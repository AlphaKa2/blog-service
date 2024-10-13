package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.dto.response.BlogPostListResponse;
import com.alphaka.blogservice.dto.response.PostDetailResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping
    public ApiResponse<Long> createPost(HttpServletRequest httpRequest,
                                                @RequestBody PostCreateRequest request) {
        Long response = postService.createPost(httpRequest, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ApiResponse<Long> updatePost(HttpServletRequest httpRequest,
                                                @PathVariable Long postId,
                                                @RequestBody PostUpdateRequest request) {
        Long response = postService.updatePost(httpRequest, postId, request);
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

    /**
     * 특정 블로그의 게시글 목록 조회
     */
    @GetMapping("/{nickname}")
    public ApiResponse<Page<BlogPostListResponse>> getBlogPostList(@PathVariable("nickname") String nickname,
                                                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                                                  @RequestParam(value = "size", defaultValue = "5") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page - 1);
        Page<BlogPostListResponse> response = postService.getBlogPostList(nickname, pageable);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(HttpServletRequest httpRequest,
                                                         @PathVariable Long postId) {
        PostDetailResponse response = postService.getPostDetails(httpRequest, postId);
        return new ApiResponse<>(response);
    }
}