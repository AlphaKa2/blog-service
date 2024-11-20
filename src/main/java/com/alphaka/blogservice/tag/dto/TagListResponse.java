package com.alphaka.blogservice.tag.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TagListResponse {
    private String tagName;
    private int postCount;
}
