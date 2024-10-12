package com.alphaka.blogservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

    @NotNull(message = "댓글 ID는 필수입니다.")
    private Long commentId;

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    @NotNull(message = "댓글 내용을 입력해주세요.")
    @Max(value = 500, message = "댓글은 최대 500자까지 입력 가능합니다.")
    private String content;

    private boolean isPublic;
}
