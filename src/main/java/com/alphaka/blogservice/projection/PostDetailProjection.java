package com.alphaka.blogservice.projection;

import java.time.LocalDateTime;

public interface PostDetailProjection {
    Long getPostId();
    Long getAuthorId();
    String getTitle();
    String getContent();
    Long getLikeCount();
    Integer getViewCount();
    Boolean getIsPublic();
    Boolean getIsLiked();
    LocalDateTime getCreatedAt();
}
