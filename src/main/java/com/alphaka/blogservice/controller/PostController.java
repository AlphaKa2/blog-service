package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.PostListResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping
    public ApiResponse<Long> createPost(CurrentUser currentUser,
                                        @Valid @RequestBody PostCreateRequest request) {
        Long response = postService.createPost(currentUser, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정 데이터 조회
     */
    @GetMapping("/{postId}/edit")
    public ApiResponse<PostUpdateRequest> getPostUpdateData(CurrentUser currentUser,
                                                            @PathVariable("postId") Long postId) {
        PostUpdateRequest response = postService.getPostUpdateData(currentUser, postId);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ApiResponse<Long> updatePost(CurrentUser currentUser,
                                        @PathVariable("postId") Long postId,
                                        @Valid @RequestBody PostUpdateRequest request) {
        Long response = postService.updatePost(currentUser, postId, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(CurrentUser currentUser,
                                        @PathVariable("postId") Long postId) {
        postService.deletePost(currentUser, postId);
        return new ApiResponse<>(null);
    }

    /**
     * 특정 블로그의 게시글 목록 조회 (페이징, 정렬 default: 최신순)
     * latest: 최신순, oldest: 오래된순, views: 조회수 많은순, likes: 좋아요 많은순
     */
    @GetMapping("/blog/{nickname}")
    public ApiResponse<Map<String, Object>> getBlogPostList(@Nullable CurrentUser currentUser,
                                                               @PathVariable("nickname") String nickname,
                                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                                               @RequestParam(value = "size", defaultValue = "5") int size,
                                                               @RequestParam(value = "sort", defaultValue = "latest") String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, getSort(sort));
        List<PostListResponse> postListResponse = postService.getPostListResponse(currentUser, nickname, pageable);

        // 페이징 정보 생성
        Map<String, Object> response = new HashMap<>();
        response.put("content", postListResponse);
        response.put("pageNumber", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("isFirst", pageable.getPageNumber() == 0);
        response.put("isLast", postListResponse.size() < pageable.getPageSize());

        return new ApiResponse<>(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostDetail(HttpServletRequest request,
                                                   @Nullable CurrentUser currentUser,
                                                   @PathVariable("postId") Long postId) {
        PostResponse response = postService.getPostResponse(request, currentUser, postId);
        return new ApiResponse<>(response);
    }

//    /**
//     * 최근 인기 게시글 목록 추천
//     */
//    @GetMapping("/popular")
//    public ApiResponse<List<PostListResponse>> getPopularPosts() {
//        List<PostListResponse> response = postService.getPopularPosts();
//        return new ApiResponse<>(response);
//    }

    // 정렬 기준에 따른 Sort 객체 반환
    private Sort getSort(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.ASC, "createdAt"); // 기본값은 최신순
        };
    }
}