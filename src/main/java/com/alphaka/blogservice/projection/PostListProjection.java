package com.alphaka.blogservice.projection;

import java.time.LocalDateTime;

/**
 * 게시글 목록 조회용 프로젝션
 * DB에서 필요한 필드만 조회하여 반환하기 위한 프로젝션
 */
public interface PostListProjection {
    Long getPostId();
    String getTitle();
    String getContent();
    Long getLikeCount();
    Integer getCommentCount();
    Integer getViewCount();
    boolean getVisible();
    boolean getCommentable();
    LocalDateTime getCreatedAt();
}
