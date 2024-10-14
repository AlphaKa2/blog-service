package com.alphaka.blogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BlogTagListResponse {
    private String tagName;
    private int postCount;
}
