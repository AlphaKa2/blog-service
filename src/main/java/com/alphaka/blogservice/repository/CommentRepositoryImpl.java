package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.QComment;
import com.alphaka.blogservice.entity.QLike;
import com.alphaka.blogservice.projection.CommentProjectionImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

public class CommentRepositoryImpl implements CommentRepositoryCustom {

    JPAQueryFactory queryFactory;

    public CommentRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 게시글에 대한 부모 댓글 목록 조회
    @Override
    public List<CommentProjectionImpl> findParentCommentsByPostId(Long postId, boolean includePrivateComments, Long userId) {
        QComment comment = QComment.comment;
        QLike like = QLike.like;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.comment.id.eq(comment.id));

        // 부모 댓글 조회
        JPAQuery<CommentProjectionImpl> query = queryFactory
                .select(Projections.constructor(CommentProjectionImpl.class,
                        comment.id.as("commentId"),
                        comment.userId.as("userId"),
                        comment.content.as("content"),
                        likeCount,
                        comment.createdAt.as("createdAt")
                ))
                .from(comment)
                .where(comment.post.id.eq(postId)
                        .and(comment.parent.isNull())) // 부모 댓글만 조회
                .orderBy(comment.createdAt.asc());

        // 비공개 댓글 포함 여부에 따라 쿼리 조건 추가
        if (!includePrivateComments) {
            if (userId != null) {
                query.where(comment.isPublic.isTrue()
                        .or(comment.userId.eq(userId))); // 유저 아이디가 있을 경우
            } else {
                query.where(comment.isPublic.isTrue()); // 유저 아이디가 없을 경우
            }
        }

        return query.fetch();
    }

    // 부모 댓글에 대한 자식 댓글 목록 조회
    @Override
    public List<CommentProjectionImpl> findChildCommentsByParentId(Long parentId, boolean includePrivateComments, Long userId) {
        QComment comment = QComment.comment;
        QLike like = QLike.like;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.comment.id.eq(comment.id));

        // 자식 댓글 조회
        JPAQuery<CommentProjectionImpl> query = queryFactory
                .select(Projections.constructor(CommentProjectionImpl.class,
                        comment.id.as("commentId"),
                        comment.userId.as("userId"),
                        comment.content.as("content"),
                        likeCount,
                        comment.createdAt.as("createdAt")
                ))
                .from(comment)
                .where(comment.parent.id.eq(parentId))
                .orderBy(comment.createdAt.asc());

        // 비공개 댓글 포함 여부에 따라 쿼리 조건 추가
        if (!includePrivateComments) {
            if (userId != null) {
                query.where(comment.isPublic.isTrue()
                        .or(comment.userId.eq(userId))); // 유저 아이디가 있을 경우
            } else {
                query.where(comment.isPublic.isTrue()); // 유저 아이디가 없을 경우
            }
        }

        return query.fetch();
    }
}
