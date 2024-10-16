package com.alphaka.blogservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;  // blogId 대신 사용

    @NotBlank(message = "제목은 필수입니다.")
    @Max(value = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private boolean isPublic;
    private boolean isCommentable;

    private List<MultipartFile> images;
    private List<MultipartFile> videos;

    private List<String> tagNames;
}