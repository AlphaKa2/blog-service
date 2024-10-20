package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.projection.PostDetailProjection;
import com.alphaka.blogservice.projection.PostListProjection;
import com.alphaka.blogservice.projection.PostListProjectionImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepositoryCustom {

    // 블로그 ID로 게시글 목록 조회 (페이징)
    Page<PostListProjection> findPostsByBlogId(Long blogId, Long blogOwnerId, boolean isOwner, Pageable pageable);

    // 게시글 ID로 게시글 상세 조회
    PostDetailProjection findPostDetailById(Long postId);

    // 태그 조회 메서드 추가
    List<String> findTagsByPostId(Long postId);

    // 인기 게시글 조회 (좋아요순 9개)
    List<PostListProjectionImpl> findPopularPosts();
}