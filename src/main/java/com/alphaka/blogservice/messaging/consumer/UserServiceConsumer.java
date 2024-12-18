package com.alphaka.blogservice.messaging.consumer;

import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * User-Service에서 발생하는 이벤트를 구독하는 컨슈머
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceConsumer {

    private final BlogService blogService;

    /**
     * 사용자 가입 이벤트를 수신하여 새로운 사용자의 블로그를 생성
     * @param userId - 사용자 ID
     */
    @KafkaListener(topics = "user-signup", groupId = "blog-service")
    public void consumeUserSignupEvent(String userId) {
        log.info("user-signup 이벤트 수신: {}", userId);
        try {
            Long parsedUserId = Long.parseLong(userId);
            blogService.createBlogForNewUser(parsedUserId);
            log.info("블로그 생성 완료: User ID: {}", parsedUserId);
        } catch (NumberFormatException e) {
            log.error("잘못된 사용자 ID: {}", userId, e);
            throw e;
        } catch (BlogCreationFailedException e) {
            log.error("블로그 생성 실패: User ID: {} - {}", userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: User ID: {} - {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 사용자 탈퇴 이벤트를 수신하여 사용자의 블로그를 삭제
     * @param userId - 사용자 ID
     */
//    @KafkaListener(topics = "user-withdrawal", groupId = "blog-service")
//    public void consumeUserWithdrawalEvent(String userId) {
//        log.info("user-withdrawal 이벤트 수신: {}", userId);
//        try {
//            Long parsedUserId = Long.parseLong(userId);
//            blogService.deleteBlogForUser(parsedUserId);
//            log.info("블로그 삭제 완료: User ID: {}", parsedUserId);
//        } catch (NumberFormatException e) {
//            log.error("잘못된 사용자 ID: {}", userId, e);
//            throw e;
//        } catch (Exception e) {
//            log.error("알 수 없는 오류 발생: User ID: {} - {}", userId, e.getMessage(), e);
//            throw e;
//        }
//    }
}