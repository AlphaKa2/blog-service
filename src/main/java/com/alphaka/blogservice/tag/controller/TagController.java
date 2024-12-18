package com.alphaka.blogservice.tag.controller;

import com.alphaka.blogservice.common.response.ApiResponse;
import com.alphaka.blogservice.tag.dto.TagListResponse;
import com.alphaka.blogservice.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 태그 목록 조회 for 블로그
     */
    @GetMapping("/blog/{nickname}")
    public ApiResponse<List<TagListResponse>> getTagListForBlog(@PathVariable("nickname") String nickname) {
        List<TagListResponse> response = tagService.getTagListForBlog(nickname);
        return new ApiResponse<>(response);
    }
}
