package com.alphaka.blogservice.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    @Builder
    public PageResponse(List<T> content, int totalPages, long totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public boolean isEmpty() {
        return (this.content == null || this.content.isEmpty());
    }
}
