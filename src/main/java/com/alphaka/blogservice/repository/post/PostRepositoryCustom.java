package com.alphaka.blogservice.repository.post;

import com.alphaka.blogservice.dto.response.PostListResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepositoryCustom {

    // 블로그 ID로 게시글 목록 조회 (페이징)
    List<PostListResponse> getPostListResponse(Long blogId, boolean isOwner, Pageable pageable);

    // 게시글 ID로 게시글 상세 조회
    Optional<PostResponse> getPostResponse(Long postId, Long userId);

    // 전체 게시글 키워드 검색 (페이징)
    List<PostListResponse> searchPosts(String keyword, boolean isOwner, Pageable pageable);

    // 인기 게시글 조회 (좋아요순 9개)
//    List<PostListProjectionImpl> findPopularPosts();
}