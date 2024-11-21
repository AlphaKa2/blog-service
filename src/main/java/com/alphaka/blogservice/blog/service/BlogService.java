package com.alphaka.blogservice.blog.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
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
     * 사용자 가입시 블로그 생성
     * @param userId - 사용자 ID
     */
    @Transactional
    public void createBlogForNewUser(Long userId) {
        log.info("사용자 ID[{}]에 대한 블로그 생성을 시작합니다.", userId);

        // 블로그 생성을 위한 검증
        blogValidation(userId);

        // 블로그 생성
        Blog newBlog = Blog.builder()
                .userId(userId)
                .build();
        blogRepository.save(newBlog);
        log.info("사용자 ID[{}]에 대한 블로그 생성이 완료 되었습니다. ", userId);
    }

    /**
     * 사용자 탈퇴시 블로그 삭제
     * @param userId - 사용자 ID
     */
//    @Transactional
//    public void deleteBlogForUser(Long userId) {
//        log.info("사용자 ID[{}]에 대한 블로그 삭제를 시작합니다.", userId);
//
//        // 블로그 삭제를 위한 검증
//        blogValidation(userId);
//
//        /* 논리적 블로그 삭제 (블로그의 게시글, 댓글, 태그도 모두 논리적 삭제)*/
//
//        log.info("사용자 ID[{}]에 대한 블로그 삭제가 완료 되었습니다. ", userId);
//    }

    // 블로그 생성을 위한 검증
    private void blogValidation(Long userId) {
        boolean isBlogExist = blogRepository.existsByUserId(userId);

        if (userId == null || userId <= 0) {
            log.error("유효하지 않은 사용자입니다. ID[{}]", userId);
            throw new UserNotFoundException();
        }

        if (isBlogExist) {
            log.warn("사용자 ID[{}]의 블로그가 이미 존재합니다.", userId);
            throw new BlogCreationFailedException();
        }
    }
}
