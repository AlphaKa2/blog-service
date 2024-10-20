package com.alphaka.blogservice.projection;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 게시글 목록 조회용 프로젝션
 * DB에서 필요한 필드만 조회하여 반환하기 위한 프로젝션
 */
@Setter
@NoArgsConstructor
public class PostListProjectionImpl implements PostListProjection{
    private Long postId;
    private String title;
    private String content;
    private Long likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private boolean visible;
    private boolean commentable;
    private LocalDateTime createdAt;

    public PostListProjectionImpl(Long postId, String title, String content, Long likeCount, Integer commentCount, Integer viewCount,
                                  boolean visible, boolean commentable, LocalDateTime createdAt) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.visible = visible;
        this.commentable = commentable;
        this.createdAt = createdAt;
    }

    @Override
    public Long getPostId() {
        return postId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Long getLikeCount() {
        return likeCount;
    }

    @Override
    public Integer getCommentCount() {
        return commentCount;
    }

    @Override
    public Integer getViewCount() {
        return viewCount;
    }

    @Override
    public boolean getVisible() {
        return visible;
    }

    @Override
    public boolean getCommentable() {
        return commentable;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
