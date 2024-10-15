package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Comment;
import com.alphaka.blogservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    // 특정 게시글의 부모 댓글 모두 조회
    List<Comment> findByPostAndParentIsNull(Post post);
}
