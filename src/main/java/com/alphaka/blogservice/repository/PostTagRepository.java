package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    // 게시글을 사용하여 게시글 태그 조회
    List<PostTag> findByPost(Post post);

    // 게시글에 해당하는 특정 태그 이름들 조회
    List<PostTag> findByPostAndTag_TagNameIn(Post post, List<String> tagNames);
}
