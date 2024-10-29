package com.alphaka.blogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotNull(message = "댓글 내용을 입력해주세요.")
    @Size(min = 1, max = 500, message = "댓글은 최소 1자, 최대 500자까지 입력 가능합니다.")
    private String content;

    private boolean isPublic;

    @Builder
    public CommentUpdateRequest(String content, boolean isPublic) {
        this.content = content;
        this.isPublic = isPublic;
    }
}
