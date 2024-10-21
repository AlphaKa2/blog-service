package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.projection.CommentProjectionImpl;

import java.util.List;

public interface CommentRepositoryCustom {

    // 게시글에 대한 부모 댓글 목록 조회
    List<CommentProjectionImpl> findParentCommentsByPostId(Long postId, boolean includePrivateComments, Long userId);

    // 부모 댓글에 대한 자식 댓글 목록 조회
    List<CommentProjectionImpl> findChildCommentsByParentId(Long parentId, boolean includePrivateComments, Long userId);
}
