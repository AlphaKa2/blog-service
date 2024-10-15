package com.alphaka.blogservice.projection;

import com.alphaka.blogservice.entity.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회용 프로젝션
 * DB에서 필요한 필드만 조회하여 반환하기 위한 프로젝션
 */
@Getter
@Setter
@NoArgsConstructor
public class PostDetailProjectionImpl implements PostDetailProjection {
    private Long postId;
    private Long authorId;
    private String title;
    private String content;
    private int likeCount;
    private int viewCount;
    private Boolean isPublic;
    private List<Tag> tags;
    private LocalDateTime createdAt;

    @Builder
    public PostDetailProjectionImpl(Long postId, Long authorId, String title, String content, int likeCount,
                                    List<Tag> tags, int viewCount, Boolean isPublic, LocalDateTime createdAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.tags = tags;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }
}
