package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // 사용자 ID로 블로그 조회
    Blog findByUserId(Long userId);
}
