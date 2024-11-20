package com.alphaka.blogservice.messaging.consumer;

import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * User Signup Kafka Consumer
 * User-Service에서 User가 가입하면 해당 User의 블로그를 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupConsumer {

    private final BlogService blogService;

    /**
     * user-signup 토픽을 구독하고 메시지를 수신하여 블로그 생성
     * @param userId - 새로 가입한 User의 ID
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
}