package com.alphaka.blogservice.repository.comment;

import com.alphaka.blogservice.dto.response.CommentResponse;

import java.util.List;

public interface CommentRepositoryCustom {

    // 게시글에 대한 부모 댓글 목록 조회
    List<CommentResponse> getParentCommentResponse(Long postId, Long userId);
}
