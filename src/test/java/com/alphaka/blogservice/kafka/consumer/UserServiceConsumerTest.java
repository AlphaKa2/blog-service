package com.alphaka.blogservice.kafka.consumer;

import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.service.BlogService;
import com.alphaka.blogservice.messaging.consumer.UserServiceConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceConsumerTest {

    @Mock
    private BlogService blogService;
    
    @InjectMocks
    private UserServiceConsumer userServiceConsumer;
    
    @Test
    @DisplayName("UserSignup 이벤트 consume 성공")
    void consumeUserSignupEvent_success() {
        // given
        String userId = "1";

        // when
        userServiceConsumer.consumeUserSignupEvent(userId);

        // then
        verify(blogService, times(1)).createBlogForNewUser(1L);
    }

    @Test
    @DisplayName("UserSignup 이벤트 consume 실패 - 잘못된 사용자 ID")
    void consumeUserSignupEvent_fail_invalidUserId() {
        // given
        String userId = "invalid";

        // when & then
        NumberFormatException exception = assertThrows(NumberFormatException.class, () -> {
                    userServiceConsumer.consumeUserSignupEvent(userId);
                });

        assertNotNull(exception);
        verify(blogService, never()).createBlogForNewUser(anyLong());
    }

    @Test
    @DisplayName("UserSignup 이벤트 consume 실패 - 블로그 생성 실패")
    void consumeUserSignupEvent_fail_BlogCreationFailed() {
        // given
        String userId = "2";
        doThrow(new BlogCreationFailedException()).when(blogService).createBlogForNewUser(2L);

        // when & then
        BlogCreationFailedException exception = assertThrows(BlogCreationFailedException.class, () -> {
                    userServiceConsumer.consumeUserSignupEvent(userId);
                });

        assertNotNull(exception);
        verify(blogService, times(1)).createBlogForNewUser(2L);
    }
}