package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름으로 태그 조회
    Optional<Tag> findByTagName(String tagName);
}
