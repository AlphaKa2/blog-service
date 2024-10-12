package com.alphaka.blogservice.Mapper;

import com.alphaka.blogservice.dto.request.UserProfile;
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
            @Mapping(source = "post.id", target = "postId"),
            @Mapping(source = "content", target = "content"),
            @Mapping(source = "userProfile.nickname", target = "nickname"),
            @Mapping(source = "isPublic", target = "isPublic"),
            @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    })
    CommentResponse toResponse(Comment comment, UserProfile userProfile);
}
