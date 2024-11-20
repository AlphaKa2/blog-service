package com.alphaka.blogservice.post.controller;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.post.dto.PostRequest;
import com.alphaka.blogservice.post.dto.PostListResponse;
import com.alphaka.blogservice.post.dto.PostResponse;
import com.alphaka.blogservice.post.service.PostService;
import com.alphaka.blogservice.util.S3Utils;
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

    private final S3Utils s3Utils;
    private final PostService postService;

    /**
     * 게시글 작성
     * @param currentUser - 현재 사용자 정보
     * @param request - 게시글 작성 요청 데이터
     */
    @PostMapping
    public ApiResponse<Long> createPost(CurrentUser currentUser,
                                        @Valid @RequestBody PostRequest request) {
        Long response = postService.createPost(currentUser, request);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정 데이터 조회
     */
    @GetMapping("/{postId}/edit")
    public ApiResponse<PostRequest> getPostUpdateData(CurrentUser currentUser,
                                                            @PathVariable("postId") Long postId) {
        PostRequest response = postService.getPostUpdateData(currentUser, postId);
        return new ApiResponse<>(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ApiResponse<Long> updatePost(CurrentUser currentUser,
                                        @PathVariable("postId") Long postId,
                                        @Valid @RequestBody PostRequest request) {
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

    /**
     * 전체 게시글 키워드 검색 (페이징, 정렬 default: 최신순)
     */
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchPosts(@Nullable CurrentUser currentUser,
                                                        @RequestParam(value = "keyword") String keyword,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @RequestParam(value = "size", defaultValue = "5") int size,
                                                        @RequestParam(value = "sort", defaultValue = "latest") String sort) {
        // 키워드가 최소 2글자인지 확인
        if (keyword == null || keyword.trim().length() < 2) {
            throw new IllegalArgumentException("키워드는 최소 2글자 이상이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, size, getSort(sort));
        List<PostListResponse> postListResponse = postService.searchPosts(currentUser, keyword, pageable);

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
     * 이미지 업로드를 위한 S3 Presigned URL 생성
     */
    @PostMapping("/presigned-url")
    public ApiResponse<Map<String, String>> getPresignedUrl(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        String contentType = request.get("contentType");

        // 서명된 URL 생성
        String presignedUrl = s3Utils.generatePresignedUrl(fileName, contentType);

        // 응답 데이터를 Map으로 작성
        Map<String, String> response = Map.of("url", presignedUrl);

        // ApiResponse로 감싸서 반환
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