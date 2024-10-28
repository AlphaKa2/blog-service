package com.alphaka.blogservice.repository.blog;

import com.alphaka.blogservice.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    /**
     * 주어진 사용자 ID로 블로그가 존재하는지 확인
     * @param userId - 사용자 ID
     * @return 블로그가 존재하면 true, 아니면 false
     */
    boolean existsByUserId(Long userId);

    // 사용자 ID로 블로그 조회
    Optional<Blog> findByUserId(Long userId);
}
