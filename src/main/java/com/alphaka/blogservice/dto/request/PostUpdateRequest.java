package com.alphaka.blogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostUpdateRequest {

    @NotNull(message = "Blog ID는 필수입니다.")
    private Long blogId;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private boolean isPublic;
    private boolean isCommentable;

    private List<MultipartFile> images;
    private List<MultipartFile> videos;

    private List<String> tagNames;
}