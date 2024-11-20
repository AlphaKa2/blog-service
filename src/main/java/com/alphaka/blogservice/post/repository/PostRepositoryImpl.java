package com.alphaka.blogservice.post.repository;

import com.alphaka.blogservice.comment.entity.QComment;
import com.alphaka.blogservice.like.entity.QLike;
import com.alphaka.blogservice.post.dto.PostListResponse;
import com.alphaka.blogservice.post.dto.PostResponse;
import com.alphaka.blogservice.post.entity.QPost;
import com.alphaka.blogservice.util.QueryDslUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 게시글 목록 조회
    @Override
    public List<PostListResponse> getPostListResponse(Long blogId, boolean isOwner, Pageable pageable) {
        QPost post = QPost.post;
        QLike like = QLike.like;
        QComment comment = QComment.comment;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.post.id.eq(post.id));

        // 댓글 수 서브쿼리
        Expression<Long> commentCount = JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(comment.post.id.eq(post.id));

        // 게시글 목록 조회
        JPAQuery<PostListResponse> query = queryFactory
                .select(Projections.constructor(PostListResponse.class,
                        post.id.as("postId"),
                        post.title,
                        post.content,
                        // 썸네일, 태그는 서비스에서 처리
                        likeCount,
                        commentCount,
                        post.viewCount,
                        post.createdAt,
                        post.updatedAt
                ))
                .from(post)
                .where(post.blog.id.eq(blogId));

        // 블로그 소유자일 경우 비공개 게시글도 조회
        if (!isOwner) {
            query.where(post.isPublic.isTrue());
        }

        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtils.getAllOrderSpecifiers(pageable, "post");
        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        }

        // 페이징 적용
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        // 결과 조회
        return query.fetch();
    }

    // 게시글 상세 조회
    @Override
    public Optional<PostResponse> getPostResponse(Long postId, Long userId) {
        QPost post = QPost.post;
        QLike like = QLike.like;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.post.id.eq(post.id));

        // 현재 사용자의 좋아요 여부 서브쿼리
        Expression<Boolean> isLiked = JPAExpressions
                .select(like.count().gt(0))
                .from(like)
                .where(like.post.id.eq(post.id).and(like.userId.eq(userId)))
                .exists();

        // 게시글 상세 조회
        PostResponse postResponse = queryFactory
                .select(Projections.constructor(PostResponse.class,
                        post.id.as("postId"),
                        post.userId.as("authorId"),
                        post.title,
                        post.content,
                        likeCount,
                        post.viewCount,
                        isLiked,
                        post.isPublic,
                        post.isCommentable,
                        post.createdAt,
                        post.updatedAt
                ))
                .from(post)
                .where(post.id.eq(postId))
                .fetchOne();

        return Optional.ofNullable(postResponse);
    }

    // 게시글 키워드 검색 (페이징)
    @Override
    public List<PostListResponse> searchPosts(String keyword, boolean isOwner, Pageable pageable) {
        QPost post = QPost.post;
        QLike like = QLike.like;
        QComment comment = QComment.comment;

        // 좋아요 수 서브쿼리
        Expression<Long> likeCount = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.post.id.eq(post.id));

        // 댓글 수 서브쿼리
        Expression<Long> commentCount = JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(comment.post.id.eq(post.id));

        // 키워드 검색 조건 추가 (제목은 대소문자 구분 없이, 내용은 대소문자 구분하여 검색)
        BooleanExpression keywordCondition = post.title.containsIgnoreCase(keyword)
                .or(post.content.contains(keyword));

        // 게시글 검색
        JPAQuery<PostListResponse> query = queryFactory
                .select(Projections.constructor(PostListResponse.class,
                        post.id.as("postId"),
                        post.title,
                        post.content,
                        likeCount,
                        commentCount,
                        post.viewCount,
                        post.createdAt,
                        post.updatedAt
                ))
                .from(post)
                .where(keywordCondition);

        // 비공개 게시글 필터링 (소유자가 아닐 경우)
        if (!isOwner) {
            query.where(post.isPublic.isTrue());
        }

        // 정렬 적용
        List<OrderSpecifier<?>> orderSpecifiers = QueryDslUtils.getAllOrderSpecifiers(pageable, "post");
        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        }

        // 페이징 적용
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        // 결과 조회
        return query.fetch();
    }

//    // 인기 게시글 목록 (좋아요 순 9개 조회)
//    @Override
//    public List<PostListProjectionImpl> findPopularPosts() {
//        QPost post = QPost.post;
//        QLike like = QLike.like;
//
//        List<PostListProjectionImpl> response = queryFactory
//                .select(Projections.constructor(PostListProjectionImpl.class,
//                        post.id.as("postId"),
//                        post.title.as("title"),
//                        post.content.as("content"),
//                        like.countDistinct().as("likeCount"),
//                        post.comments.size().as("commentCount"),
//                        post.viewCount.as("viewCount"),
//                        post.isPublic.as("visible"),
//                        post.isCommentable.as("commentable"),
//                        post.createdAt.as("createdAt")
//                ))
//                .from(post)
//                .leftJoin(like).on(like.post.eq(post))
//                .where(post.isPublic.isTrue())
//                .groupBy(post.id, post.title, post.content, post.viewCount, post.isPublic, post.isCommentable, post.createdAt)
//                .orderBy(like.count().desc())
//                .limit(9)                               // 상위 9개만 가져옴
//                .fetch();
//
//
//        return response;
//    }
}