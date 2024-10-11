package com.alphaka.blogservice.Mapper;

import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    Post toEntity(PostCreateRequest request);
    PostResponse toResponse(Post post);
}