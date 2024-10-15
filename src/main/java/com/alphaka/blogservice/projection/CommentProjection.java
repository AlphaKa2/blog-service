package com.alphaka.blogservice.projection;

import java.time.LocalDateTime;

public interface CommentProjection {
    Long getCommentId();
    Long getAuthorId();
    String getContent();
    Long getLikeCount();
    LocalDateTime getCreatedAt();
}
