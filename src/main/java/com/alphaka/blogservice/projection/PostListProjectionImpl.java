package com.alphaka.blogservice.projection;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Long commentCount;
    private Integer viewCount;
    private boolean visible;
    private boolean commentable;
    private LocalDateTime createdAt;
    private List<String> tags;

    public PostListProjectionImpl(Long postId, String title, String content, Long likeCount, Long commentCount, Integer viewCount,
                                  boolean visible, boolean commentable, LocalDateTime createdAt, String tagName) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.visible = visible;
        this.commentable = commentable;
        this.createdAt = createdAt;
        this.tags = new ArrayList<>();
        if (!tagName.isEmpty()) {
            this.tags.add(tagName);
        }
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
    public Long getCommentCount() {
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

    @Override
    public List<String> getTags() {
        return tags;
    }
}
