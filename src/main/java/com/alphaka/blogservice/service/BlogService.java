package com.alphaka.blogservice.service;

import com.alphaka.blogservice.entity.Blog;
import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    // 블로그 생성
    @Transactional
    public void createBlog(Long userId) {
        log.info("Blog creation for user: {}", userId);

        // 블로그 생성 로직 (실패시 롤백)
        try {
            Blog blog = new Blog(userId);
            blogRepository.save(blog);
            log.info("Blog created: {}", blog);
        } catch (Exception e) {
            log.error("Blog creation failed: {}", e.getMessage());
            throw new BlogCreationFailedException();
        }
    }

}
