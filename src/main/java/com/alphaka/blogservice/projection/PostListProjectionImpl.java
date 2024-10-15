package com.alphaka.blogservice.projection;

import lombok.Getter;
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
@Getter
@NoArgsConstructor
public class PostListProjectionImpl implements PostListProjection{
    private Long postId;
    private String title;
    private String content;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private LocalDateTime createdAt;
    private List<String> tags;

    public PostListProjectionImpl(Long postId, String title, String content, int likeCount, int commentCount,
                                  int viewCount, LocalDateTime createdAt, String tagName) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.tags = new ArrayList<>();
        if (!tagName.isEmpty()) {
            this.tags.add(tagName);
        }
    }
}
