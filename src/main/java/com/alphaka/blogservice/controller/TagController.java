package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.dto.response.BlogTagListResponse;
import com.alphaka.blogservice.service.TagService;
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
    public ApiResponse<List<BlogTagListResponse>> getTagListForBlog(@PathVariable("nickname") String nickname) {
        List<BlogTagListResponse> response = tagService.getTagListForBlog(nickname);
        return new ApiResponse<>(response);
    }
}
