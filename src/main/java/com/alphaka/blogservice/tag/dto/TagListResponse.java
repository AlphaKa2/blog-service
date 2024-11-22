package com.alphaka.blogservice.tag.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagListResponse {
    private String tagName;
    private int postCount;
}
