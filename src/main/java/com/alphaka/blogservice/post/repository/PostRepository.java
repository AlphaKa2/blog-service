package com.alphaka.blogservice.post.repository;

import com.alphaka.blogservice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 게시글 조회수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void increaseViewCount(@Param("postId") Long postId);

    // 댓글 ID로 게시글 조회
    Optional<Post> findByCommentsId(Long commentId);

    // 모든 공개 게시글 수 조회
    Long countByIsPublicTrue();
}