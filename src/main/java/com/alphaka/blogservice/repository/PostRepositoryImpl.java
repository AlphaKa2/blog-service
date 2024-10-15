package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.*;
import com.alphaka.blogservice.projection.PostDetailProjection;
import com.alphaka.blogservice.projection.PostDetailProjectionImpl;
import com.alphaka.blogservice.projection.PostListProjection;
import com.alphaka.blogservice.projection.PostListProjectionImpl;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    // 블로그 ID로 게시글 목록 조회 (페이징)
    @Override
    public Page<PostListProjection> findPostsByBlogId(Long blogOwnerId, boolean isOwner, Pageable pageable) {
        QPost post = QPost.post;
        QLike like = QLike.like;
        QComment comment = QComment.comment;
        QTag tag = QTag.tag;
        QPostTag postTag = QPostTag.postTag;

        /* 게시글 기본 정보 조회 쿼리
        필요한 필드만 선택해서 조회한다.
        게시글 ID, 작성자 ID, 제목, 내용, 좋아요 수, 댓글 수, 조회수, 작성일
        */
        List<PostListProjectionImpl> content = queryFactory
                .select(Projections.fields(PostListProjectionImpl.class,
                        post.id.as("postId"),
                        post.title,
                        post.content,
                        post.viewCount.as("viewCount"),
                        post.createdAt.as("createdAt"),
                        post.userId.as("authorId"), // 작성자 ID
                        // 좋아요 수 조회 (서브쿼리)
                        ExpressionUtils.as(
                                JPAExpressions.select(like.count())
                                        .from(like)
                                        .where(like.post.id.eq(post.id)),
                                "likeCount"
                        ),
                        // 댓글 수 조회 (서브쿼리)
                        ExpressionUtils.as(
                                JPAExpressions.select(comment.count())
                                        .from(comment)
                                        .where(comment.post.id.eq(post.id)),
                                "commentCount"
                        )
                ))
                .from(post)
                .where( // 블로그 주인의 게시글만 조회
                        post.userId.eq(blogOwnerId)
                        .and(isOwner ? null : post.isPublic.isTrue()))
                .orderBy(getOrderSpecifiers(pageable.getSort(), post)) // 정렬 조건 설정
                .offset(pageable.getOffset())  // 페이징 설정 (offset)
                .limit(pageable.getPageSize()) // 페이징 설정 (limit)
                .fetch();

        // 게시글이 없을 경우 빈 페이지 반환
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 게시글 ID 목록 추출 (태그 조회용)
        List<Long> postIds = content.stream()
                .map(PostListProjectionImpl::getPostId)
                .collect(Collectors.toList());

        // 태그 목록 조회 및 매핑 (게시글 ID가 일치하는 태그만 조회)
        List<Tuple> tagTuples = queryFactory
                .select(postTag.post.id, tag.tagName)
                .from(postTag)
                .join(postTag.tag, tag) // PostTag 테이블과 Tag 테이블 조인
                .where(postTag.post.id.in(postIds)) // 게시글 ID 목록에 해당하는 태그만 조회
                .fetch();

        // 태그를 게시글 ID에 매핑 (앞서 조회한 태그 목록을 게시글 ID로 매핑)
        Map<Long, List<String>> postTagsMap = new HashMap<>();
        for (Tuple tuple : tagTuples) {
            Long postId = tuple.get(postTag.post.id); // 게시글 ID
            String tagName = tuple.get(tag.tagName);  // 태그 이름
            postTagsMap.computeIfAbsent(postId, k -> new ArrayList<>()).add(tagName); // 게시글 ID에 태그 추가
        }

        // 조회된 게시글 리스트를 순회하며, 각 게시글에 해당하는 태그 목록을 설정
        for (PostListProjectionImpl projection : content) {
            List<String> tags = postTagsMap.getOrDefault(projection.getPostId(), Collections.emptyList());
            projection.setTags(tags);
        }

        // 전체 게시글 수 조회
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(post.userId.eq(blogOwnerId)
                        .and(isOwner ? null : post.isPublic.isTrue())
                )
                .fetchOne();

        // 최종적으로 조회된 게시글 리스트와 전체 게시글 수로 페이지 객체 생성
        return new PageImpl<>(new ArrayList<>(content), pageable, total);
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

    // 정렬 조건 설정 (Sort 객체를 OrderSpecifier 배열로 변환)
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, QPost post) {
        return sort.stream()
                .map(order -> {
                    PathBuilder<?> path = new PathBuilder<>(post.getType(), post.getMetadata());
                    return new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, path.get(order.getProperty()));
                })
                .toArray(OrderSpecifier[]::new);
    }
}
