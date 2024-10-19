package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Tag;

import java.util.List;

public interface TagRepositoryCustom {
    // 태그 엔티티를 일괄 저장
    void batchInsert(List<Tag> tags);
}
