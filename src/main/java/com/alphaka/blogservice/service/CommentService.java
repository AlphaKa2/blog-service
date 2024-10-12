package com.alphaka.blogservice.service;

import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.entity.Comment;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.ParentCommentNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.repository.CommentRepository;
import com.alphaka.blogservice.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 댓글 작성
     * @param httpRequest HttpServletRequest
     * @param request 댓글 작성 요청
     */
    @Transactional
    public void createComment(HttpServletRequest httpRequest, CommentCreateRequest request) {
        log.info("댓글 작성 요청 - Post ID: {}", request.getPostId());

        Long userId = getAuthenticatedUserId(httpRequest);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        // 부모 댓글이 있을 경우 확인
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId()).orElseThrow(ParentCommentNotFoundException::new);
        }

        // 댓글 생성
        Comment comment = Comment.builder()
                .userId(userId)
                .post(post)
                .content(request.getContent())
                .parent(parentComment)
                .isPublic(request.isPublic())
                .build();

        commentRepository.save(comment);
        log.info("댓글 작성 완료 - Comment ID: {}", comment.getId());
    }

    // 댓글 수정

    // 댓글 삭제

    /**
     * 현재 인증된 사용자 ID를 추출하고 확인
     * @param request HttpServletRequest
     * @return 사용자 ID
     */
    private Long getAuthenticatedUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-USER-ID");

        if (userIdHeader == null) {
            log.error("헤더에서 사용자 정보를 찾을 수 없습니다.");
            throw new UnauthorizedException();
        }

        // 사용자 ID가 숫자인지 확인
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("인증된 사용자 ID: {}", userId);
            return userId;
        } catch (NumberFormatException e) {
            log.error("헤더의 사용자 ID가 유효하지 않습니다: {}", userIdHeader);
            throw new UnauthorizedException();
        }
    }
}
