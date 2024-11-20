package com.alphaka.blogservice.comment.repository;

import com.alphaka.blogservice.comment.dto.CommentResponse;

import java.util.List;

public interface CommentRepositoryCustom {

    // 게시글에 대한 부모 댓글 목록 조회
    List<CommentResponse> getParentCommentResponse(Long postId, Long userId);
}
