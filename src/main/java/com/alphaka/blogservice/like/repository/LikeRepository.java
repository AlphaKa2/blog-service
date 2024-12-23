package com.alphaka.blogservice.like.repository;

import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.like.entity.Like;
import com.alphaka.blogservice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // 사용자 ID와 게시글로 좋아요 정보 조회
    Optional<Like> findByUserIdAndPost(Long userId, Post post);

    // 사용자 ID와 댓글로 좋아요 정보 조회
    Optional<Like> findByUserIdAndComment(Long userId, Comment comment);
}
