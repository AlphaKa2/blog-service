package com.alphaka.blogservice.Mapper;

import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    // Post -> PostResponse 매핑
    @Mappings({
            @Mapping(source = "blog.id", target = "blogId"),           // Blog ID 매핑
            @Mapping(source = "blog.nickname", target = "nickname"),   // Blog의 닉네임 매핑
            @Mapping(source = "userId", target = "userId"),            // 작성자 ID 매핑
            @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss"),  // 작성일시 포맷팅
            @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")   // 수정일시 포맷팅
    })
    PostResponse toResponse(Post post);
}