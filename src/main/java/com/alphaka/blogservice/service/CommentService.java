package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.dto.request.CommentCreateRequest;
import com.alphaka.blogservice.dto.request.CommentUpdateRequest;
import com.alphaka.blogservice.dto.request.UserInfo;
import com.alphaka.blogservice.dto.request.UserProfile;
import com.alphaka.blogservice.dto.response.CommentResponse;
import com.alphaka.blogservice.entity.Comment;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.*;
import com.alphaka.blogservice.projection.CommentProjectionImpl;
import com.alphaka.blogservice.repository.CommentRepository;
import com.alphaka.blogservice.repository.LikeRepository;
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
    private final LikeRepository likeRepository;

    /**
     * 특정 게시글의 댓글 조회
     * @param httpRequest HTTP 요청
     * @param postId 게시글 ID
     * @return List<CommentDetailResponse> 댓글 목록
     */
    public List<CommentResponse> getCommentsForPost(HttpServletRequest httpRequest, Long postId) {
        log.info("특정 게시글의 댓글 조회 - Post ID: {}", postId);

        // 현재 사용자 확인
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(httpRequest);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // 게시글 작성자인지 확인
        boolean isPostAuthor = currentUser != null && currentUser.getUserId().equals(post.getUserId());

        // 부모 댓글 조회
        List<CommentProjectionImpl> parentProjections = commentRepository.findParentCommentsByPostId(
                postId, isPostAuthor, currentUser != null ? currentUser.getUserId() : null
        );

        // 부모 댓글을 DTO로 변환하면서 작성자 정보 추가
        List<CommentResponse> parentComments = parentProjections.stream()
                .map(parentComment -> {
                    UserInfo author = userClient.findUser(parentComment.getAuthorId()).getData();

                    // 자식 댓글 재귀적으로 조회
                    List<CommentResponse> childComments = getChildrenComments(
                            parentComment.getCommentId(), isPostAuthor, currentUser != null ? currentUser.getUserId() : null
                    );

                    // 좋아요 여부 확인
                    boolean isLiked = currentUser != null && likeRepository.existsByUserIdAndComment(
                            currentUser.getUserId(), commentRepository.findById(parentComment.getCommentId()).orElseThrow(CommentNotFoundException::new)
                    );

                    // 부모 댓글과 자식 댓글들을 함께 매핑
                    return mapToCommentResponse(parentComment, author, childComments, isLiked);
                })
                .collect(Collectors.toList());

        log.info("특정 게시글의 댓글 조회 완료 - Post ID: {}", postId);
        return parentComments;
    }

    /**
     * 부모 댓글에 대한 자식 댓글 재귀적으로 조회
     * @param parentId 부모 댓글 ID
     * @param includePrivateComments 비공개 댓글 포함 여부
     * @param userId 현재 로그인한 사용자 ID
     * @return 자식 댓글 목록
     */
    private List<CommentResponse> getChildrenComments(Long parentId, boolean includePrivateComments, Long userId) {
        // 부모 댓글에 대한 자식 댓글 조회
        List<CommentProjectionImpl> childProjections = commentRepository.findChildCommentsByParentId(parentId, includePrivateComments, userId);

        // 자식 댓글들을 재귀적으로 조회하여 대댓글 포함
        return childProjections.stream()
                .map(childComment -> {
                    UserInfo childAuthor = userClient.findUser(childComment.getAuthorId()).getData();
                    List<CommentResponse> grandchildren = getChildrenComments(childComment.getCommentId(), includePrivateComments, userId); // 대댓글 조회
                    // 좋아요 여부 확인
                    boolean isLiked = likeRepository.existsByUserIdAndComment(
                            userId, commentRepository.findById(childComment.getCommentId()).orElseThrow(CommentNotFoundException::new)
                    );
                    return mapToCommentResponse(childComment, childAuthor, grandchildren, isLiked);  // 자식 댓글과 대댓글 함께 매핑
                })
                .collect(Collectors.toList());
    }

    /**
     * 댓글 작성
     * @param httpRequest HTTP 요청
     * @param request 댓글 작성 요청
     * @return CommentResponse 작성된 댓글 응답
     */
    @Transactional
    public Long createComment(HttpServletRequest httpRequest, CommentCreateRequest request) {
        log.info("댓글 작성 요청 - Post ID: {}", request.getPostId());

        // 현재 사용자 확인
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(httpRequest);
        if (currentUser == null) {
            log.error("로그인 해야 댓글을 작성할 수 있습니다.");
            throw new SignInRequiredException();
        }

        // 댓글 작성하려는 게시글 존재 확인
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        // 부모 댓글이 있을 경우 확인
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId()).orElseThrow(ParentCommentNotFoundException::new);
        }

        // 댓글 생성
        Comment comment = Comment.builder()
                .userId(currentUser.getUserId())
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
     * @param httpRequest HTTP 요청
     * @param commentId 댓글 ID
     * @param request 댓글 수정 요청
     * @return CommentResponse 수정된 댓글 응답
     */
    @Transactional
    public Long updateComment(HttpServletRequest httpRequest, Long commentId, CommentUpdateRequest request) {
        log.info("댓글 수정 요청 - Comment ID: {}", commentId);

        // 헤더에서 사용자 정보 추출
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(httpRequest);

        // 댓글 수정 권한 확인
        Comment comment = validateCommentOwnership(commentId, currentUser.getUserId());

        // 댓글 수정
        comment.updateComment(request.getContent(), request.isPublic());
        commentRepository.save(comment);
        log.info("댓글 수정 완료 - Comment ID: {}", comment.getId());

        return comment.getId();
    }

    /**
     * 댓글 삭제
     * @param httpRequest HTTP 요청
     * @param commentId   댓글 ID
     */
    @Transactional
    public void deleteComment(HttpServletRequest httpRequest, Long commentId) {
        log.info("댓글 삭제 요청 - Comment ID: {}", commentId);

        // 헤더에서 사용자 정보 추출
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(httpRequest);

        // 댓글 삭제 권한 확인
        Comment comment = validateCommentOwnership(commentId, currentUser.getUserId());

        // 댓글 삭제
        commentRepository.delete(comment);
        log.info("댓글 삭제 완료 - Comment ID: {}", commentId);
    }

    /**
     * CommentProjectionImpl -> CommentResponse로 변환하는 매핑 함수
     * @param commentProjection 댓글 프로젝션
     * @param author 작성자 정보
     * @param children 자식 댓글 목록
     * @return 댓글 응답 DTO
     */
    private CommentResponse mapToCommentResponse(CommentProjectionImpl commentProjection, UserInfo author,
                                                 List<CommentResponse> children, boolean isLiked) {
        return CommentResponse.builder()
                .commentId(commentProjection.getCommentId())
                .authorNickname(author.getNickname())
                .authorProfileImage(author.getProfileImage())
                .content(commentProjection.getContent())
                .likeCount(commentProjection.getLikeCount().intValue())
                .children(children)  // 자식 댓글 리스트
                .isLiked(isLiked)
                .createdAt(commentProjection.getCreatedAt())
                .build();
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
