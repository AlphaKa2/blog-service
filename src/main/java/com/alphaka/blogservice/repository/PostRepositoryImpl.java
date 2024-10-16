package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.*;
import com.alphaka.blogservice.projection.PostDetailProjection;
import com.alphaka.blogservice.projection.PostDetailProjectionImpl;
import com.alphaka.blogservice.projection.PostListProjection;
import com.alphaka.blogservice.projection.PostListProjectionImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 블로그 ID로 게시글 목록 조회 (페이징)
    @Override
    public Page<PostListProjection> findPostsByBlogId(Long blogId, Long blogOwnerId, boolean isOwner, Pageable pageable) {
        QPost post = QPost.post;
        QLike like = QLike.like;
        QComment comment = QComment.comment;
        QPostTag postTag = QPostTag.postTag;

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

        // 기본 쿼리
        JPAQuery<PostListProjectionImpl> query = queryFactory
                .selectDistinct(Projections.constructor(PostListProjectionImpl.class,
                        post.id.as("postId"),
                        post.title.as("title"),
                        post.content.as("content"),
                        likeCount,  // 서브쿼리로 조회된 좋아요 수
                        commentCount,  // 서브쿼리로 조회된 댓글 수
                        post.viewCount.as("viewCount"),
                        post.isPublic.as("visible"),
                        post.isCommentable.as("commentable"),
                        post.createdAt.as("createdAt")
                ))
                .from(post)
                .leftJoin(post.postTags, postTag)
                .where(post.blog.id.eq(blogId));

        // 소유자가 아니라면 공개된 게시글만 조회
        if (!isOwner) {
            query.where(post.isPublic.isTrue());
        }

        // 정렬 기준 적용
        applySorting(query, pageable);

        // 페이징 처리
        List<PostListProjection> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(postProjection -> (PostListProjection) postProjection)  // 변환
                .toList();

        // 전체 게시글 수 조회
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(post.blog.id.eq(blogId))
                .fetchOne();

        // Page 객체로 변환하여 반환
        return new PageImpl<>(content, pageable, total);
    }

    // 게시글 ID로 게시글 상세 조회
    @Override
    public PostDetailProjection findPostDetailById(Long postId) {
        QPost post = QPost.post;
        QLike like = QLike.like;

        // 게시글 상세 정보 조회 (tags 제외)
        PostDetailProjectionImpl response = queryFactory
                .select(Projections.constructor(PostDetailProjectionImpl.class,
                        post.id.as("postId"),
                        post.userId.as("authorId"),
                        post.title.as("title"),
                        post.content.as("content"),
                        // 좋아요 수 조회 (서브쿼리)
                        JPAExpressions.select(like.count())
                                .from(like)
                                .where(like.post.id.eq(post.id)),
                        post.viewCount.as("viewCount"),
                        post.isPublic.as("isPublic"),
                        post.createdAt.as("createdAt")
                ))
                .from(post)
                .where(post.id.eq(postId))
                .fetchOne();

        return response;
    }

    @Override
    public List<String> findTagsByPostId(Long postId) {
        QTag tag = QTag.tag;
        QPostTag postTag = QPostTag.postTag;

        // 게시글 ID로 태그 목록 조회
        return queryFactory
                .select(tag.tagName)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch();
    }

    // 정렬 기준 적용
    private void applySorting(JPAQuery<?> query, Pageable pageable) {
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            PathBuilder<?> pathBuilder = new PathBuilder<>(QPost.post.getType(), QPost.post.getMetadata());

            switch (order.getProperty()) {
                case "createdAt" -> {
                    PathBuilder<LocalDateTime> createdAtPath = pathBuilder.get("createdAt", LocalDateTime.class);
                    if (order.isAscending()) {
                        query.orderBy(new OrderSpecifier<>(Order.ASC, createdAtPath));
                    } else {
                        query.orderBy(new OrderSpecifier<>(Order.DESC, createdAtPath));
                    }
                }
                case "viewCount" -> {
                    PathBuilder<Integer> viewCountPath = pathBuilder.get("viewCount", Integer.class);
                    if (order.isAscending()) {
                        query.orderBy(new OrderSpecifier<>(Order.ASC, viewCountPath));
                    } else {
                        query.orderBy(new OrderSpecifier<>(Order.DESC, viewCountPath));
                    }
                }
                case "likeCount" -> {
                    PathBuilder<Long> likeCountPath = pathBuilder.get("likeCount", Long.class);
                    if (order.isAscending()) {
                        query.orderBy(new OrderSpecifier<>(Order.ASC, likeCountPath));
                    } else {
                        query.orderBy(new OrderSpecifier<>(Order.DESC, likeCountPath));
                    }
                }
                default -> {
                    // 기본 정렬 필드 (createdAt 기준)
                    PathBuilder<LocalDateTime> defaultPath = pathBuilder.get("createdAt", LocalDateTime.class);
                    if (order.isAscending()) {
                        query.orderBy(new OrderSpecifier<>(Order.ASC, defaultPath));
                    } else {
                        query.orderBy(new OrderSpecifier<>(Order.DESC, defaultPath));
                    }
                }
            }
        }
    }
}