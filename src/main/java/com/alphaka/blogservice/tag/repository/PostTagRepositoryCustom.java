package com.alphaka.blogservice.tag.repository;

import com.alphaka.blogservice.tag.entity.PostTag;

import java.util.List;

public interface PostTagRepositoryCustom {
    // PostTag 엔티티를 일괄 저장
    void batchInsert(List<PostTag> postTags);
}
