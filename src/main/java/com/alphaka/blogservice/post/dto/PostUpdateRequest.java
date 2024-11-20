package com.alphaka.blogservice.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @JsonProperty("isPublic")
    private boolean isPublic;

    @JsonProperty("isCommentable")
    private boolean isCommentable;

    private List<String> tagNames;

    @Builder
    public PostUpdateRequest(String title, String content, boolean isPublic, boolean isCommentable, List<String> tagNames) {
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        this.isCommentable = isCommentable;
        this.tagNames = tagNames;
    }
}