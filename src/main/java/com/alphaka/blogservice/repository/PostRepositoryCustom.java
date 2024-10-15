package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.projection.PostDetailProjection;
import com.alphaka.blogservice.projection.PostListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepositoryCustom {

    // 블로그 ID로 게시글 목록 조회 (페이징)
    Page<PostListProjection> findPostsByBlogId(Long blogOwnerId, boolean isOwner, Pageable pageable);

    // 게시글 ID로 게시글 상세 조회
    PostDetailProjection findPostDetailById(Long postId);
}