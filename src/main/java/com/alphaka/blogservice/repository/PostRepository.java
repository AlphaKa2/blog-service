package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 블로그 ID로 게시글 좋아요, 댓글 수 조회
    @Query("SELECT p.id as postId, count(DISTINCT l.id) as likeCount, count(DISTINCT c.id) as commentCount " +
            "FROM Post p " +
            "LEFT JOIN Like l ON l.post.id = p.id " +
            "LEFT JOIN Comment c ON c.post.id = p.id " +
            "WHERE p.blog.id = :blogId " +
            "GROUP BY p.id")
    List<Object[]> findPostLikeAndCommentCountsByBlogId(@Param("blogId") Long blogId);
}