package com.alphaka.blogservice.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    @NotNull(message = "댓글 내용을 입력해주세요.")
    @Size(min = 1, max = 500, message = "댓글은 최소 1자, 최대 500자까지 입력 가능합니다.")
    private String content;

    private Long parentId;

    private boolean isPublic = true;
}
