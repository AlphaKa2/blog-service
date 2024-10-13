package com.alphaka.blogservice.service;

import com.alphaka.blogservice.Mapper.CommentMapper;
import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.dto.request.CommentUpdateRequest;
import com.alphaka.blogservice.dto.request.UserProfile;
import com.alphaka.blogservice.dto.response.CommentDetailResponse;
import com.alphaka.blogservice.dto.response.CommentResponse;
import com.alphaka.blogservice.entity.Comment;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.CommentNotFoundException;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final UserClient userClient;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserProfileService userProfileService;
    private final CommentMapper commentMapper = CommentMapper.INSTANCE;

    /**
     * 특정 게시글의 댓글 조회
     * @param postId 게시글 ID
     * @return List<CommentDetailResponse> 댓글 목록
     */
    public List<CommentResponse> getCommentsForPost(Long postId) {
        log.info("특정 게시글의 댓글 조회 - Post ID: {}", postId);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // 부모 댓글 조회 (부모 댓글은 parent가 null인 댓글)
        List<Comment> parentComments = commentRepository.findByPostAndParentIsNull(post);

        // 부모 댓글을 DTO로 변환 및 자식 댓글 포함
        List<CommentResponse> response = parentComments.stream()
                .filter(Comment::isPublic)  // 공개된 댓글만 필터링
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("댓글 조회 완료 - Post ID: {}, 댓글 수: {}", postId, response.size());
        return response;
    }

    /**
     * 댓글 엔티티를 DTO로 변환하고 자식 댓글 포함
     * @param comment 부모 댓글 엔티티
     * @return CommentDetailResponse 부모 및 자식 댓글 정보
     */
    private CommentResponse mapToResponse(Comment comment) {
        // 작성자 프로필 이미지와 닉네임을 UserClient를 통해 가져오기
        String nickname = userClient.findNicknameByUserId(comment.getUserId());
        String profileImage = userClient.findProfileImageByUserId(comment.getUserId());

        // 자식 댓글 재귀적으로 처리
        List<CommentResponse> children = comment.getChildren().stream()
                .filter(Comment::isPublic)  // 공개된 자식 댓글만 필터링
                .map(this::mapToResponse)   // 자식 댓글을 재귀적으로 처리
                .collect(Collectors.toList());

        // 부모 댓글 정보와 자식 댓글 리스트를 포함한 DTO 생성
        return CommentResponse.builder()
                .commentId(comment.getId())
                .authorNickname(nickname)
                .authorProfileImage(profileImage)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikes().size())  // 좋아요 수
                .children(children)  // 자식 댓글 리스트
                .build();
    }

    /**
     * 댓글 작성
     * @param httpRequest HttpServletRequest
     * @param request 댓글 작성 요청
     * @return CommentResponse 작성된 댓글 응답
     */
    @Transactional
    public Long createComment(HttpServletRequest httpRequest, CommentCreateRequest request) {
        log.info("댓글 작성 요청 - Post ID: {}", request.getPostId());

        // 헤더에서 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        // 부모 댓글이 있을 경우 확인
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId()).orElseThrow(ParentCommentNotFoundException::new);
        }

        // 댓글 생성
        Comment comment = Comment.builder()
                .userId(userProfile.getUserId())
                .post(post)
                .content(request.getContent())
                .parent(parentComment)
                .isPublic(request.isPublic())
                .build();

        commentRepository.save(comment);
        log.info("댓글 작성 완료 - Comment ID: {}", comment.getId());

        return comment.getId();
    }

    /**
     * 댓글 수정
     * @param httpRequest HttpServletRequest
     * @param commentId 댓글 ID
     * @param request 댓글 수정 요청
     * @return CommentResponse 수정된 댓글 응답
     */
    @Transactional
    public Long updateComment(HttpServletRequest httpRequest, Long commentId, CommentUpdateRequest request) {
        log.info("댓글 수정 요청 - Comment ID: {}", commentId);

        // 헤더에서 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 댓글 수정 권한 확인
        Comment comment = validateCommentOwnership(commentId, userProfile.getUserId());

        // 댓글 수정
        comment.updateComment(request.getContent(), request.isPublic());
        commentRepository.save(comment);
        log.info("댓글 수정 완료 - Comment ID: {}", comment.getId());

        return comment.getId();
    }

    /**
     * 댓글 삭제
     * @param httpRequest HttpServletRequest
     * @param commentId   댓글 ID
     */
    @Transactional
    public void deleteComment(HttpServletRequest httpRequest, Long commentId) {
        log.info("댓글 삭제 요청 - Comment ID: {}", commentId);

        // 헤더에서 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 댓글 삭제 권한 확인
        Comment comment = validateCommentOwnership(commentId, userProfile.getUserId());

        // 댓글 삭제
        commentRepository.delete(comment);
        log.info("댓글 삭제 완료 - Comment ID: {}", commentId);
    }

    /**
     * 댓글 작성자인지 확인
     * @param commentId 댓글 ID
     * @param userId 사용자 ID
     * @return Comment 객체
     */
    private Comment validateCommentOwnership(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

        if (!comment.getUserId().equals(userId)) {
            log.error("댓글 작성자가 아닙니다 - Comment ID: {}, User ID: {}", comment.getId(), userId);
            throw new UnauthorizedException();
        }

        return comment;
    }
}
