package com.alphaka.blogservice.projection;

import java.time.LocalDateTime;
import java.util.List;

public interface PostDetailProjection {
    Long getPostId();
    Long getAuthorId();
    String getTitle();
    String getContent();
    List<String> getTags();
    int getLikeCount();
    int getViewCount();
    LocalDateTime getCreatedAt();
    Boolean getIsPublic();
}
