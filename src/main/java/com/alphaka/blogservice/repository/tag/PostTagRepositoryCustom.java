package com.alphaka.blogservice.repository.tag;

import com.alphaka.blogservice.entity.PostTag;

import java.util.List;

public interface PostTagRepositoryCustom {
    // PostTag 엔티티를 일괄 저장
    void batchInsert(List<PostTag> postTags);
}
