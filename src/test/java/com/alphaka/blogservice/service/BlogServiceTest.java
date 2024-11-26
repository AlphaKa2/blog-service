package com.alphaka.blogservice.service;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.blog.service.BlogService;
import com.alphaka.blogservice.exception.custom.BlogCreationFailedException;
import com.alphaka.blogservice.blog.repository.BlogRepository;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;

    @Test
    @DisplayName("새로운 사용자를 위한 블로그 생성 성공")
    void createBlogForNewUser_success() {
        // given
        Long userId = 1L;
        when(blogRepository.existsByUserId(userId)).thenReturn(false);
        Blog savedBlog = Blog.builder().userId(1L).build();
        when(blogRepository.save(any(Blog.class))).thenReturn(savedBlog);

        // when
        blogService.createBlogForNewUser(userId);

        // then
        verify(blogRepository, times(1)).existsByUserId(userId);
        verify(blogRepository, times(1)).save(any(Blog.class));
    }

    @Test
    @DisplayName("새로운 사용자를 위한 블로그 생성 실패 - 블로그 이미 존재")
    void createBlogForNewUser_fail_BlogAlreadyExists() {
        // Given
        Long userId = 2L;
        when(blogRepository.existsByUserId(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> blogService.createBlogForNewUser(userId))
                .isInstanceOf(BlogCreationFailedException.class);

        verify(blogRepository, times(1)).existsByUserId(userId);
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    @DisplayName("새로운 사용자를 위한 블로그 생성 실패 - 사용자 ID가 null")
    void createBlogForNewUser_fail_InvalidUserId_Null() {
        // Given
        Long userId = null;

        // When & Then
        assertThatThrownBy(() -> blogService.createBlogForNewUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(blogRepository, never()).existsByUserId(anyLong());
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    @DisplayName("새로운 사용자를 위한 블로그 생성 실패 - 사용자 ID가 음수")
    void createBlogForNewUser_fail_InvalidUserId_Negative() {
        // Given
        Long userId = -1L;

        // When & Then
        assertThatThrownBy(() -> blogService.createBlogForNewUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    @DisplayName("새로운 사용자를 위한 블로그 생성 실패 - DB 에러")
    void createBlogForNewUser_fail_SaveThrowsException() {
        // Given
        Long userId = 3L;
        when(blogRepository.existsByUserId(userId)).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenThrow(new RuntimeException("DB error"));

        // When & Then
        assertThatThrownBy(() -> blogService.createBlogForNewUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(blogRepository, times(1)).existsByUserId(userId);
        verify(blogRepository, times(1)).save(any(Blog.class));
    }
}