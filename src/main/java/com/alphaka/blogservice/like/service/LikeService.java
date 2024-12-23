package com.alphaka.blogservice.like.service;

import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.like.entity.Like;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.exception.custom.CommentNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.like.repository.LikeRepository;
import com.alphaka.blogservice.post.repository.PostRepository;
import com.alphaka.blogservice.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {

    private final CacheUtils cacheUtils;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    /**
     * 게시글 좋아요 또는 좋아요 취소
     * @param currentUser - 현재 사용자 정보
     * @param postId - 게시글 ID
     */
    @Transactional
    public void toggleLikeOnPost(CurrentUser currentUser, Long postId) {
        log.info("게시글 좋아요 토글 - Post ID: {}", postId);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Long userId = currentUser.getUserId();

        // 좋아요 여부 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndPost(userId, post);
        log.info("좋아요 정보 - Post ID: {}, User ID: {}, Like: {}", postId, userId, existingLike.isPresent());

        // 좋아요 토글
        if (existingLike.isPresent()) {
            // 이미 좋아요가 눌려있는 경우 좋아요 취소
            likeRepository.delete(existingLike.get());
            log.info("게시글 좋아요 취소 - Post ID: {}, User ID: {}", postId, userId);
        } else {
            // 좋아요가 눌려있지 않은 경우 좋아요 추가
            Like like = Like.builder()
                    .userId(userId)
                    .post(post)
                    .comment(null)
                    .build();
            likeRepository.save(like);
            log.info("게시글 좋아요 - Post ID: {}, User ID: {}", postId, userId);
        }

        // 좋아요 변경 시, 관련 캐시 무효화
        cacheUtils.evictLikeCountForPost(postId);
        cacheUtils.evictUserLikeOnPost(userId, postId);

        // 좋아요 변경 시, 게시글 상세 캐시 삭제
        cacheUtils.evictPostDetailsCache(postId, userId);

        // 좋아요 변경 시, 블로그 게시글 목록 캐시 삭제
        Long blogId = post.getBlog().getId();
        cacheUtils.evictPostListCache(blogId);
    }

    /**
     * 댓글 좋아요 또는 좋아요 취소
     * @param currentUser - 현재 사용자 정보
     * @param commentId - 댓글 ID
     */
    @Transactional
    public void toggleLikeOnComment(CurrentUser currentUser, Long commentId) {
        log.info("댓글 좋아요 토글 - Comment ID: {}", commentId);

        // 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        Long userId = currentUser.getUserId();

        // 좋아요 여부 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndComment(userId, comment);
        log.info("좋아요 정보 - Comment ID: {}, User ID: {}, Like: {}", commentId, userId, existingLike.isPresent());

        // 좋아요 토글
        if (existingLike.isPresent()) {
            // 이미 좋아요가 눌려있는 경우 좋아요 취소
            likeRepository.delete(existingLike.get());
            log.info("댓글 좋아요 취소 - Comment ID: {}, User ID: {}", commentId, userId);
        } else {
            // 좋아요가 눌려있지 않은 경우 좋아요 추가
            Like like = Like.builder()
                    .userId(userId)
                    .post(null)
                    .comment(comment)
                    .build();
            likeRepository.save(like);
            log.info("댓글 좋아요 - Comment ID: {}, User ID: {}", commentId, userId);
        }

        // 댓글 좋아요 변경 시, 관련 캐시 무효화
        cacheUtils.evictLikeCountForComment(commentId);
        cacheUtils.evictUserLikeOnComment(userId, commentId);

        // 댓글이 속한 게시글의 댓글 캐시 삭제
        Post post = comment.getPost();
        cacheUtils.evictCommentsCache(post.getId());
    }
}
