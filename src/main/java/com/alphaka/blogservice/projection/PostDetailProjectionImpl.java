package com.alphaka.blogservice.projection;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 게시글 상세 조회용 프로젝션
 * DB에서 필요한 필드만 조회하여 반환하기 위한 프로젝션
 */
@Setter
@NoArgsConstructor
public class PostDetailProjectionImpl implements PostDetailProjection {
    private Long postId;
    private Long authorId;
    private String title;
    private String content;
    private Long likeCount;
    private Integer viewCount;
    private Boolean isPublic;
    private Boolean isLiked;
    private LocalDateTime createdAt;

    public PostDetailProjectionImpl(Long postId, Long authorId, String title, String content, Long likeCount,
                                    Integer viewCount, Boolean isPublic, Boolean isLiked, LocalDateTime createdAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.isPublic = isPublic;
        this.isLiked = isLiked;
        this.createdAt = createdAt;
    }

    @Override
    public Long getPostId() {
        return postId;
    }

    @Override
    public Long getAuthorId() {
        return authorId;
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
    public Integer getViewCount() {
        return viewCount;
    }

    @Override
    public Boolean getIsPublic() {
        return isPublic;
    }

    @Override
    public Boolean getIsLiked() {
        return isLiked;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
