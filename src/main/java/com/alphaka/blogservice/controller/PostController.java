package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.dto.response.PostListResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                                        @Valid @RequestBody PostCreateRequest request) {
        Long response = postService.createPost(httpRequest, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ApiResponse<Long> updatePost(HttpServletRequest httpRequest,
                                        @PathVariable("postId") Long postId,
                                        @Valid @RequestBody PostUpdateRequest request) {
        Long response = postService.updatePost(httpRequest, postId, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(HttpServletRequest request,
                                        @PathVariable("postId") Long postId) {
        postService.deletePost(request, postId);
        return new ApiResponse<>(null);
    }

    /**
     * 특정 블로그의 게시글 목록 조회 (페이징, 정렬 default: 최신순)
     * latest: 최신순, oldest: 오래된순, views: 조회수 많은순, likes: 좋아요 많은순
     */
    @GetMapping("/blog/{nickname}")
    public ApiResponse<Page<PostListResponse>> getBlogPostList(HttpServletRequest httpRequest,
                                                               @PathVariable("nickname") String nickname,
                                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                                               @RequestParam(value = "size", defaultValue = "5") int size,
                                                               @RequestParam(value = "sort", defaultValue = "latest") String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, getSort(sort));
        Page<PostListResponse> response = postService.getBlogPostList(httpRequest, nickname, pageable);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostDetail(HttpServletRequest httpRequest,
                                                   @PathVariable("postId") Long postId) {
        PostResponse response = postService.getPostDetails(httpRequest, postId);
        return new ApiResponse<>(response);
    }

    // 정렬 기준에 따른 Sort 객체 반환
    private Sort getSort(String sort) {
        return switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 기본값은 최신순
        };
    }
}