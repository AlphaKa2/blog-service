package com.alphaka.blogservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "blogService:cache";

    /**
     * Redis 키 패턴에 따라 키를 삭제하는 공통 메서드
     * @param pattern - 삭제할 키 패턴
     * @param logMessage - 로그 메시지
     * @param args - 로그 메시지 인자
     */
    private void deleteKeysByPattern(String pattern, String logMessage, Object... args) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info(logMessage, args);
        }
    }

    // 특정 블로그의 게시글 목록 캐시 무효화
    public void evictPostListCache(Long blogId) {
        String pattern = String.format("%s:postList::blog:%d:*", PREFIX, blogId);
        deleteKeysByPattern(pattern, "블로그 ID {}의 게시글 목록 캐시가 초기화 되었습니다.", blogId);
    }

    // 특정 블로그 태그 목록 캐시 무효화
    public void evictTagListCache(Long blogId) {
        String pattern = String.format("%s:tagList::blog:%d:*", PREFIX, blogId);
        deleteKeysByPattern(pattern, "블로그 ID {}의 태그 목록 캐시가 초기화 되었습니다.", blogId);
    }

    // 특정 게시글의 댓글 캐시 무효화
    public void evictCommentsCache(Long postId) {
        String pattern = String.format("%s:comments::post:%d", PREFIX, postId);
        deleteKeysByPattern(pattern, "게시글 ID {}의 댓글 캐시가 초기화 되었습니다.", postId);
    }

    // 특정 게시글 상세 조회 캐시 무효화
    public void evictPostDetailsCache(Long postId) {
        String key = String.format("%s:postDetails::post:%d", PREFIX, postId);
        redisTemplate.delete(key);
        log.info("게시글 ID {}의 상세 정보 캐시가 초기화 되었습니다.", postId);
    }

    // 특정 게시글의 좋아요 수 캐시 무효화
    public void evictLikeCountForPost(Long postId) {
        String key = String.format("%s:likeCount::post:%d", PREFIX, postId);
        redisTemplate.delete(key);
        log.info("게시글 ID {}의 좋아요 수 캐시가 초기화 되었습니다.", postId);
    }

    // 특정 댓글의 좋아요 수 캐시 무효화
    public void evictLikeCountForComment(Long commentId) {
        String key = String.format("%s:likeCount::comment:%d", PREFIX, commentId);
        redisTemplate.delete(key);
        log.info("댓글 ID {}의 좋아요 수 캐시가 초기화 되었습니다.", commentId);
    }

    // 특정 게시글의 사용자 좋아요 여부 캐시 무효화
    public void evictUserLikeOnPost(Long userId, Long postId) {
        String key = String.format("%s:like::post:%d:userId:%d", PREFIX, postId, userId);
        redisTemplate.delete(key);
        log.info("사용자 ID {}의 게시글 ID {} 좋아요 여부 캐시가 삭제되었습니다.", userId, postId);
    }

    // 특정 댓글의 사용자 좋아요 여부 캐시 무효화
    public void evictUserLikeOnComment(Long userId, Long commentId) {
        String key = String.format("%s:like::comment:%d:userId:%d", PREFIX, commentId, userId);
        redisTemplate.delete(key);
        log.info("사용자 ID {}의 댓글 ID {} 좋아요 여부 캐시가 삭제되었습니다.", userId, commentId);
    }

    // 특정 블로그의 게시글 목록 및 태그 목록 캐시 무효화
    public void evictPostListAndTagListCache(Long blogId) {
        evictPostListCache(blogId);
        evictTagListCache(blogId);
    }

    // 특정 게시글의 댓글 및 블로그 게시글 목록 캐시 무효화
    public void evictCommentsAndPostListCache(Long blogId, Long postId) {
        evictCommentsCache(postId);
        evictPostListCache(blogId);
    }
}
