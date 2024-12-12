package com.alphaka.blogservice.comment.service;

import com.alphaka.blogservice.client.feign.UserClient;
import com.alphaka.blogservice.comment.dto.CommentCreateRequest;
import com.alphaka.blogservice.comment.dto.CommentResponse;
import com.alphaka.blogservice.comment.dto.CommentUpdateRequest;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.comment.repository.CommentRepository;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.exception.custom.*;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.post.repository.PostRepository;
import com.alphaka.blogservice.util.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CacheUtils cacheUtils;
    private final UserClient userClient;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 댓글 작성
     * @param currentUser - 현재 사용자 정보
     * @param request - 댓글 작성 요청
     * @return Long - 작성된 댓글 ID
     */
    @Transactional
    public Long createComment(CurrentUser currentUser, CommentCreateRequest request) {
        log.info("댓글 작성 요청 - Post ID: {}", request.getPostId());

        // 댓글을 작성하려는 게시글 존재 확인
        Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        // 게시글에 댓글 작성 가능한 상태인지 검증
        if (!post.isCommentable()) {
            log.error("댓글 작성 불가능한 게시글입니다 - Post ID: {}", post.getId());
            throw new PrivateParentCommentException();
        }

        // 부모 댓글이 있을 경우 확인
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = validateParentComment(request.getParentId(), post);
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

        // 댓글 작성 후, 댓글 캐시와 블로그 게시글 목록 캐시 무효화
        Long blogId = post.getBlog().getId();
        cacheUtils.evictCommentsAndPostListAndDetailsCache(blogId, post.getId(), currentUser.getUserId());

        return comment.getId();
    }

    /**
     * 수정할 댓글 정보 조회
     * @param currentUser - 현재 사용자 정보
     * @param commentId - 댓글 ID
     * @return CommentUpdateRequest - 수정할 댓글 정보
     */
    public CommentUpdateRequest getCommentUpdateData(CurrentUser currentUser, Long commentId) {
        log.info("댓글 수정을 위한 정보 조회 - Comment ID: {}", commentId);

        // 댓글을 작성하려는 게시글 존재 확인
        Post post = postRepository.findByCommentsId(commentId).orElseThrow(PostNotFoundException::new);

        // 댓글 작성 가능 여부 확인
        if (!post.isCommentable()) {
            log.error("댓글 작성이 중단된 게시글입니다. - Post ID: {}", post.getId());
            throw new UnauthorizedException();
        }

        // 댓글 존재와 작성자인지 확인
        Comment comment = validateCommentOwnership(commentId, currentUser.getUserId());

        // 댓글 수정을 위한 정보 조회
        CommentUpdateRequest response = CommentUpdateRequest.builder()
                .content(comment.getContent())
                .isPublic(comment.isPublic())
                .build();

        log.info("댓글 수정을 위한 정보 조회 완료 - Comment ID: {}", commentId);
        return response;
    }

    /**
     * 댓글 수정
     * @param currentUser - 현재 사용자 정보
     * @param commentId - 댓글 ID
     * @param request - 댓글 수정 요청
     * @return Long - 수정된 댓글 ID
     */
    @Transactional
    public Long updateComment(CurrentUser currentUser, Long commentId, CommentUpdateRequest request) {
        log.info("댓글 수정 요청 - Comment ID: {}", commentId);

        // 댓글 수정 권한 확인
        Comment comment = validateCommentOwnership(commentId, currentUser.getUserId());

        // 댓글을 작성하려는 게시글 존재 확인
        Post post = postRepository.findByCommentsId(commentId).orElseThrow(PostNotFoundException::new);

        // 댓글 작성 가능 여부 확인
        if (!post.isCommentable()) {
            log.error("댓글 작성이 중단된 게시글입니다. - Post ID: {}", post.getId());
            throw new UnauthorizedException();
        }

        // 댓글 수정
        comment.updateComment(request.getContent(), request.isPublic());
        commentRepository.save(comment);
        log.info("댓글 수정 완료 - Comment ID: {}", comment.getId());

        // 댓글 수정 후, 댓글 캐시와 블로그 게시글 목록 캐시 무효화 (블로그 ID 사용)
        Long blogId = post.getBlog().getId();
        cacheUtils.evictCommentsAndPostListAndDetailsCache(blogId, post.getId(), currentUser.getUserId());

        return comment.getId();
    }

    /**
     * 댓글 삭제
     * @param currentUser - 현재 사용자 정보
     * @param commentId - 댓글 ID
     */
    @Transactional
    public void deleteComment(CurrentUser currentUser, Long commentId) {
        log.info("댓글 삭제 요청 - Comment ID: {}", commentId);

        // 댓글 존재와 삭제 권한 확인
        Comment comment = validateCommentOwnership(commentId, currentUser.getUserId());

        // 댓글 삭제
        commentRepository.delete(comment);
        log.info("댓글 삭제 완료 - Comment ID: {}", commentId);

        // 댓글 삭제 후, 댓글 캐시와 블로그 게시글 목록 캐시 무효화 (블로그 ID 사용)
        Post post = comment.getPost();
        Long blogId = post.getBlog().getId();
        cacheUtils.evictCommentsAndPostListAndDetailsCache(blogId, post.getId(), currentUser.getUserId());
    }

    /**
     * 댓글 작성자인지 확인
     * @param commentId - 댓글 ID
     * @param userId - 사용자 ID
     * @return Comment - 댓글 정보
     */
    private Comment validateCommentOwnership(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

        if (!comment.getUserId().equals(userId)) {
            log.error("댓글 작성자가 아닙니다 - Comment ID: {}, User ID: {}", comment.getId(), userId);
            throw new UnauthorizedException();
        }

        return comment;
    }

    /**
     * 특정 게시글의 댓글 조회
     * @param currentUser - 현재 사용자 정보
     * @param postId - 게시글 ID
     * @return List<CommentDetailResponse> - 댓글 목록
     */
    @Cacheable(value = "blogService:cache:comments", key = "'post:' + #postId", unless = "#result == null")
    public List<CommentResponse> getCommentsForPost(CurrentUser currentUser, Long postId) {
        log.info("특정 게시글의 댓글 조회 - Post ID: {}", postId);

        // 게시글 존재 여부 및 게시글 소유주 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Long userId = currentUser != null ? currentUser.getUserId() : null;

        // 모든 댓글 조회
        log.info("모든 댓글 조회");
        List<CommentResponse> comments = commentRepository.getParentCommentResponse(postId, userId);

        // 비공개 댓글 처리 및 사용자 ID 수집
        log.info("비공개 댓글 처리 및 사용자 ID 수집");
        Set<Long> authorIds = new HashSet<>();
        for (CommentResponse comment : comments) {
            // 비공개 댓글 처리
            if (!comment.isPublic()) {
                // 댓글 작성자나 게시글 작성자가 아닌 경우 내용 대체
                boolean isAuthor = userId != null && userId.equals(comment.getAuthorId());
                boolean isPostOwner = userId != null && userId.equals(post.getUserId());
                if (!isAuthor && !isPostOwner) {
                    comment.setAuthorId(null); // 작성자 ID 제거
                    comment.setAuthor("비공개 사용자");
                    comment.setAuthorProfileImage(null);
                    comment.setContent("비공개 댓글입니다.");
                    comment.setLikeCount(0L);
                    comment.setLiked(false);
                    continue; // 사용자 정보 매핑에서 제외하기 위해 다음 반복으로 넘어감
                }
            }
            // 비공개 댓글이 아니거나, 작성자나 게시글 소유자인 경우에만 사용자 ID 수집
            if (comment.getAuthorId() != null) {
                authorIds.add(comment.getAuthorId());
            }
        }

        // 사용자 정보 조회
        log.info("사용자 정보 조회");
        List<UserDTO> userDTOs = Collections.emptyList();
        if (!authorIds.isEmpty()) {
            userDTOs = userClient.getUsersById(authorIds).getData();
        }
        Map<Long, UserDTO> userMap = userDTOs.stream()
                .collect(Collectors.toMap(UserDTO::getUserId, Function.identity()));

        // 사용자 정보 매핑
        log.info("사용자 정보 매핑");
        for (CommentResponse comment : comments) {
            if (comment.getAuthorId() != null) { // 비공개 댓글이 아닌 경우에만 매핑
                UserDTO user = userMap.get(comment.getAuthorId());
                if (user != null) {
                    comment.setAuthor(user.getNickname());
                    comment.setAuthorProfileImage(user.getProfileImage());
                }
            }
        }

        // 댓글 계층 구조 생성
        List<CommentResponse> commentHierarchy = buildCommentHierarchy(comments);

        log.info("특정 게시글의 댓글 조회 완료 - Post ID: {}", postId);
        return commentHierarchy;
    }

    /**
     * 부모 댓글 검증
     * @param parentId - 부모 댓글 ID
     * @param post - 게시글 정보
     * @return Comment - 부모 댓글 정보
     */
    private Comment validateParentComment(Long parentId, Post post) {
        // 부모 댓글 존재 확인
        Comment parentComment = commentRepository.findById(parentId).orElseThrow(ParentCommentNotFoundException::new);

        // 부모 댓글이 작성하려는 게시글과 같은 게시글에 속해 있는지 검증
        if (!parentComment.getPost().getId().equals(post.getId())) {
            log.error("부모 댓글이 작성하려는 게시글과 다릅니다 - Parent Comment ID: {}, Parent Post ID: {}, Current Post ID: {}",
                    parentComment.getId(), parentComment.getPost().getId(), post.getId());
            throw new InvalidParentCommentException();
        }

        // 부모 댓글이 비공개 상태인지 검증
        if (!parentComment.isPublic()) {
            log.error("부모 댓글이 비공개 상태입니다 - Parent Comment ID: {}", parentComment.getId());
            throw new InvalidParentCommentException();
        }

        return parentComment;
    }

    /**
     * 댓글 계층 구조 생성
     * @param comments - 댓글 목록
     * @return List<CommentDetailResponse> - 댓글 계층 구조
     */
    private List<CommentResponse> buildCommentHierarchy(List<CommentResponse> comments) {
        log.info("댓글 계층구조 매핑 작업");

        // 부모 댓글을 빠르게 찾기 위해 Map의 Key값으로 설정
        Map<Long, CommentResponse> commentMap = comments.stream()
                .collect(Collectors.toMap(CommentResponse::getCommentId, Function.identity()));

        // 부모 댓글이 없는 최상위 댓글을 저장
        List<CommentResponse> rootComments = new ArrayList<>();

        for (CommentResponse comment : comments) {
            // 최상위 댓글의 경우 rootComment에 저장
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                // 최상위 댓글이 아닐 경우 부모 댓글을 조회하고 자식 댓글로 등록
                CommentResponse parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.getChildren().add(comment);
                }
            }
        }

        return rootComments;
    }
}
