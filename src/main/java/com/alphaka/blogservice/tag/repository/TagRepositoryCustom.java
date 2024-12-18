package com.alphaka.blogservice.tag.repository;

import com.alphaka.blogservice.tag.entity.Tag;

import java.util.List;
import java.util.Map;

public interface TagRepositoryCustom {
    // 태그 엔티티를 일괄 저장
    void batchInsert(List<Tag> tags);

    // 게시글 ID 리스트별 태그 조회
    Map<Long, List<String>> findTagsByPostIds(List<Long> postIds);
}
