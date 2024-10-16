package com.alphaka.blogservice.projection;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
public class CommentProjectionImpl implements CommentProjection {
    Long commentId;
    Long authorId;
    String content;
    Long likeCount;
    LocalDateTime createdAt;

    public CommentProjectionImpl(Long commentId, Long authorId, String content, Long likeCount, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.content = content;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
    }

    @Override
    public Long getCommentId() {
        return commentId;
    }

    @Override
    public Long getAuthorId() {
        return authorId;
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
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
