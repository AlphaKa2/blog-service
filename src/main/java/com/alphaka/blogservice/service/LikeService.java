package com.alphaka.blogservice.service;

import com.alphaka.blogservice.dto.request.UserProfile;
import com.alphaka.blogservice.entity.Comment;
import com.alphaka.blogservice.entity.Like;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.CommentNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.repository.CommentRepository;
import com.alphaka.blogservice.repository.LikeRepository;
import com.alphaka.blogservice.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
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

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserProfileService userProfileService;

    /**
     * 게시글 좋아요 또는 좋아요 취소
     * @param httpRequest HTTP 요청
     * @param postId 게시글 ID
     */
    @Transactional
    public void toggleLikeOnPost(HttpServletRequest httpRequest, Long postId) {
        log.info("게시글 좋아요 토글 - Post ID: {}", postId);

        // 헤더에서 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // 좋아요 여부 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndPost(userProfile.getUserId(), post);
        log.info("좋아요 정보 - Post ID: {}, User ID: {}, Like: {}", postId, userProfile.getUserId(), existingLike.isPresent());

        // 좋아요 토글
        if (existingLike.isPresent()) {
            // 이미 좋아요가 눌려있는 경우 좋아요 취소
            likeRepository.delete(existingLike.get());
            log.info("게시글 좋아요 취소 - Post ID: {}, User ID: {}", postId, userProfile.getUserId());
        } else {
            // 좋아요가 눌려있지 않은 경우 좋아요 추가
            Like like = Like.builder()
                    .userId(userProfile.getUserId())
                    .post(post)
                    .comment(null)
                    .build();
            likeRepository.save(like);
            log.info("게시글 좋아요 - Post ID: {}, User ID: {}", postId, userProfile.getUserId());
        }
    }

    /**
     * 댓글 좋아요 또는 좋아요 취소
     * @param httpRequest HTTP 요청
     * @param commentId   댓글 ID
     */
    @Transactional
    public void toggleLikeOnComment(HttpServletRequest httpRequest, Long commentId) {
        log.info("댓글 좋아요 토글 - Comment ID: {}", commentId);

        // 헤더에서 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

        // 좋아요 여부 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndComment(userProfile.getUserId(), comment);
        log.info("좋아요 정보 - Comment ID: {}, User ID: {}, Like: {}", commentId, userProfile.getUserId(), existingLike.isPresent());

        // 좋아요 토글
        if (existingLike.isPresent()) {
            // 이미 좋아요가 눌려있는 경우 좋아요 취소
            likeRepository.delete(existingLike.get());
            log.info("댓글 좋아요 취소 - Comment ID: {}, User ID: {}", commentId, userProfile.getUserId());
        } else {
            // 좋아요가 눌려있지 않은 경우 좋아요 추가
            Like like = Like.builder()
                    .userId(userProfile.getUserId())
                    .post(null)
                    .comment(comment)
                    .build();
            likeRepository.save(like);
            log.info("댓글 좋아요 - Comment ID: {}, User ID: {}", commentId, userProfile.getUserId());
        }
    }
}
