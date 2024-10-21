package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventListenerService {

    private final BlogService blogService;
    private final UserClient userClient;

    // Kafka Topic으로부터 UserSignUpEvent를 수신하여 블로그 생성
    @Transactional
    @KafkaListener(topics = "user-signup", groupId = "blog-service")
    public void handleUserSignUpEvent(String userId) {
        Long id = Long.parseLong(userId);
        log.info("UserSignUpEvent received: {}", id);

        // 유저 존재 여부 검증 (boolean 사용)l
        log.info("Checking user existence for ID: {}", id);
        if (userClient.findUserById(id).getData() == null) {
            log.error("User not found for ID: {}", id);
            throw new UserNotFoundException();
        }
        log.info("User found for ID: {}", id);

        // 블로그 생성 (실패시 롤백)
        try {
            blogService.createBlog(id);
            log.info("Blog created for user: {}", id);
        } catch (Exception e) {
            log.error("Blog creation failed: {}", e.getMessage());
            throw new BlogCreationFailedException();
        }
    }
}
