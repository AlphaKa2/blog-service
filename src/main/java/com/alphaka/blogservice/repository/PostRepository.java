package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 블로그 ID로 게시글 좋아요, 댓글 수 조회
    @Query("SELECT p.id as postId, count(DISTINCT l.id) as likeCount, count(DISTINCT c.id) as commentCount " +
            "FROM Post p " +
            "LEFT JOIN Like l ON l.post.id = p.id " +
            "LEFT JOIN Comment c ON c.post.id = p.id " +
            "WHERE p.blog.id = :blogId " +
            "GROUP BY p.id")
    Page<Object[]> findPostLikeAndCommentCountsByBlogId(@Param("blogId") Long blogId, Pageable pageable);

    // 게시글 조회수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void increaseViewCount(@Param("postId") Long postId);
}