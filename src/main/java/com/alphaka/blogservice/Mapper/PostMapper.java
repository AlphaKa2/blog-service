package com.alphaka.blogservice.Mapper;

import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    // Post -> PostResponse 매핑
    @Mappings({
            @Mapping(source = "post.id", target = "postId"),
            @Mapping(source = "nickname", target = "author"),
            @Mapping(source = "post.title", target = "title"),
            @Mapping(source = "post.content", target = "content"),
            @Mapping(source = "tags", target = "tags"),
            @Mapping(target = "likeCount", expression = "java(post.getLikes().size())"),
            @Mapping(source = "post.createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    })
    PostResponse toResponse(Post post, String nickname, List<String> tags);
}