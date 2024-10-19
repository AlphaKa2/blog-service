package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {

    // 태그 이름으로 태그 조회
    List<Tag> findByTagNameIn(List<String> tagNames);
}
