package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentDetailResponse {
    private Long commentId;
    private String authorNickname;
    private String authorProfileImage;
    private String content;
    private int likeCount;
    private List<CommentDetailResponse> children;  // 자식 댓글 리스트

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Builder
    public CommentDetailResponse(Long commentId, String authorNickname, String authorProfileImage, String content, int likeCount, List<CommentDetailResponse> children, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.authorNickname = authorNickname;
        this.authorProfileImage = authorProfileImage;
        this.content = content;
        this.likeCount = likeCount;
        this.children = children;
        this.createdAt = createdAt;
    }
}
