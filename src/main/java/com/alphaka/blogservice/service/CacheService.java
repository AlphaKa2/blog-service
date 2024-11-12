package com.alphaka.blogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 특정 블로그의 게시글 목록 캐시 무효화
    public void evictPostListCache(Long blogId) {
        String pattern = "blogService:postList::" + blogId + "-*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("블로그 ID {}의 게시글 목록 캐시가 초기화 되었습니다.", blogId);
        }
    }

    // 특정 블로그 태그 목록 캐시 무효화
    public void evictTagListCache(Long blogId) {
        String pattern = "blogService:tagList::" + blogId + "-*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("블로그 ID {}의 태그 목록 캐시가 초기화 되었습니다.", blogId);
        }
    }

    // 특정 게시글의 댓글 캐시 무효화
    public void evictCommentsCache(Long postId) {
        String pattern = "blogService:comments::" + postId;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("다음 게시글의 댓글 목록 캐시가 초기화 되었습니다. postId: {}", postId);
        }
    }

    // 특정 게시글 상세 조회 캐시 무효화
    public void evictPostDetailsCache(Long postId) {
        String key = "blogService:postDetails::" + postId;
        redisTemplate.delete(key);
        log.info("게시글 ID {}의 상세 정보 캐시가 초기화 되었습니다.", postId);
    }

    // 좋아요
    public void evictLikeCountForPost(Long postId) {
        String key = "blogService:likeCount:post::" + postId;
        redisTemplate.delete(key);
        log.info("게시글 ID {}의 상세 정보 캐시가 초기화 되었습니다.", postId);
    }

    // 좋아요 수 캐시 무효화 (댓글)
    public void evictLikeCountForComment(Long commentId) {
        String key = "blogService:likeCount:comment::" + commentId;
        redisTemplate.delete(key);
        log.info("게시글 ID {}의 상세 정보 캐시가 초기화 되었습니다.", commentId);
    }

    // 사용자 좋아요 여부 캐시 무효화 (게시글)
    public void evictUserLikeOnPost(Long userId, Long postId) {
        String key = "blogService:userLike:post::" + userId + ":" + postId;
        redisTemplate.delete(key);
        log.info("사용자 ID {}의 게시글 ID {} 좋아요 여부 캐시가 삭제되었습니다.", userId, postId);
    }

    // 사용자 좋아요 여부 캐시 무효화 (댓글)
    public void evictUserLikeOnComment(Long userId, Long commentId) {
        String key = "blogService:userLike:comment::" + userId + ":" + commentId;
        redisTemplate.delete(key);
        log.info("사용자 ID {}의 댓글 ID {} 좋아요 여부 캐시가 삭제되었습니다.", userId, commentId);
    }

    // 특정 블로그의 게시글 목록과 태그 목록 캐시 무효화
    public void evictCommentsAndPostListCache(Long blogId, Long postId) {
        evictCommentsCache(postId);
        evictPostListCache(blogId);
    }

    // 특정 게시글의 댓글과 블로그 게시글 목록 캐시 무효화
    public void evictPostListAndTagListCache(Long blogId) {
        evictPostListCache(blogId);
        evictTagListCache(blogId);
    }
}
