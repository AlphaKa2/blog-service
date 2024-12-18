package com.alphaka.blogservice.comment.repository;

import com.alphaka.blogservice.comment.dto.CommentResponse;
import com.alphaka.blogservice.comment.entity.QComment;
import com.alphaka.blogservice.like.entity.QLike;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 게시글의 모든 댓글 조회
    @Override
    public List<CommentResponse> getParentCommentResponse(Long postId, Long userId) {
        QComment comment = QComment.comment;
        QLike like = QLike.like;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.comment.id.eq(comment.id));

        // 현재 사용자 좋아요 여부 서브쿼리
        Expression<Boolean> isLiked = JPAExpressions
                .select(like.id.isNotNull())
                .from(like)
                .where(like.comment.id.eq(comment.id).and(like.userId.eq(userId)));

        // 부모 댓글 조회
        return queryFactory
                .select(Projections.constructor(CommentResponse.class,
                        comment.id.as("commentId"),
                        comment.parent.id.as("parentId"),
                        comment.userId.as("authorId"),
                        comment.content,
                        likeCount,
                        isLiked,
                        comment.isPublic,
                        comment.createdAt,
                        comment.updatedAt
                ))
                .from(comment)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.createdAt.asc())
                .fetch();
    }
}
