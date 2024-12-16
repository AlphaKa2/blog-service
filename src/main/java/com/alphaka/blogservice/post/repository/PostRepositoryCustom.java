package com.alphaka.blogservice.post.repository;

import com.alphaka.blogservice.post.dto.AllPostListResponse;
import com.alphaka.blogservice.post.dto.PostListResponse;
import com.alphaka.blogservice.post.dto.PostResponse;
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

    // 블로그 ID로 게시글 수 조회
    Long countPostsByBlogId(Long blogId, boolean isOwner);

    // 키워드로 게시글 수 조회
    Long countPostsByKeyword(String keyword, boolean isOwner);

    // 전체 공개 게시글 조회 (페이징)
    List<AllPostListResponse> findAllPublicPosts(Pageable pageable);

    // 전체 게시글 조회 (페이징) - 관리자용
//    List<PostListResponse> getPosts(Pageable pageable);

    // 인기 게시글 조회 (좋아요순 9개)
//    List<PostListProjectionImpl> findPopularPosts();
}