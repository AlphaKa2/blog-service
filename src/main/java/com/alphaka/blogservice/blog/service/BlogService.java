package com.alphaka.blogservice.blog.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    /**
     * 새로 가입한 User의 블로그 생성
     * @param userId - 새로 가입한 User의 ID
     */
    @Transactional
    public void createBlogForNewUser(Long userId) {
        log.info("블로그를 생성합니다. 사용자 ID: {}", userId);

        // 사용자와 블로그의 존재 여부 확인
        userValidation(userId);
        blogValidation(userId);

        // 블로그 생성
        Blog newBlog = Blog.builder()
                .userId(userId)
                .build();
        blogRepository.save(newBlog);
        log.info("블로그가 생성되었습니다. 사용자 ID: {}", userId);
    }

    // 블로그 유효성 검증
    private void blogValidation(Long userId) {
        boolean isBlogExist = blogRepository.existsByUserId(userId);
        if (isBlogExist) {
            log.warn("블로그가 이미 존재합니다. 사용자 ID: {}", userId);
            throw new BlogCreationFailedException();
        }
    }

    // 사용자 ID 유효성 검증
    private static void userValidation(Long userId) {
        if (userId == null || userId <= 0) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new BlogCreationFailedException();
        }
    }
}
