package com.alphaka.blogservice.Mapper;

import com.alphaka.blogservice.dto.response.CommentResponse;
import com.alphaka.blogservice.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mappings({
            @Mapping(source = "comment.id", target = "commentId"),
            @Mapping(source = "comment.post.id", target = "postId"),
            @Mapping(source = "nickname", target = "nickname"),
            @Mapping(source = "comment.content", target = "content"),
            @Mapping(target = "public", expression = "java(comment.isPublic())"),
            @Mapping(source = "comment.createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "comment.updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    })
    CommentResponse toResponse(Comment comment, String nickname);
}
