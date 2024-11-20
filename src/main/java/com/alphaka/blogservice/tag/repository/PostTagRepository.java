package com.alphaka.blogservice.tag.repository;

import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.tag.entity.PostTag;
import com.alphaka.blogservice.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long>, PostTagRepositoryCustom {

    // 게시글을 사용하여 게시글 태그 조회
    List<PostTag> findByPost(Post post);

    // 게시글에 해당하는 특정 태그 이름들 조회
    List<PostTag> findByPostAndTag_TagNameIn(Post post, List<String> tagNames);

    // 게시글의 태그 목록 조회
    @Query("SELECT pt.tag.tagName FROM PostTag pt WHERE pt.post.id = :postId")
    List<String> findTagsByPostId(@Param("postId") Long postId);

    // 블로그에 속한 게시글들의 태그 목록 조회
    @Query("SELECT pt.tag FROM PostTag pt WHERE pt.post.blog.id = :blogId")
    List<Tag> findTagsByBlogId(@Param("blogId") Long blogId);

    // 특정 블로그의 태그별 게시글 수 조회
    @Query("SELECT count(pt) FROM PostTag pt WHERE pt.post.blog.id = :blogId AND pt.tag.id = :tagId")
    int countByBlogIdAndTagId(@Param("blogId") Long blogId, @Param("tagId") Long tagId);
}
