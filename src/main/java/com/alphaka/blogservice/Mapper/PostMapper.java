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
            @Mapping(source = "post.id", target = "postId"),
            @Mapping(source = "post.blog.id", target = "blogId"),
            @Mapping(source = "nickname", target = "nickname"),
            @Mapping(source = "post.title", target = "title"),
            @Mapping(source = "post.content", target = "content"),
            @Mapping(source = "post.public", target = "isPublic"),
            @Mapping(source = "post.commentable", target = "isCommentable"),
            @Mapping(source = "post.viewCount", target = "viewCount"),
            @Mapping(source = "post.createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "post.updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(target = "tags", expression = "java(post.getPostTags().stream().map(postTag -> postTag.getTag().getTagName()).collect(java.util.stream.Collectors.toList()))") // 태그 목록 매핑
    })
    PostResponse toResponse(Post post, String nickname);
}