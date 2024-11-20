package com.alphaka.blogservice.common.constants;

/**
 * API 경로 상수를 정의하는 클래스
 */
public class ApiPaths {

    // 기본 경로
    public static final String API_BASE = "/api";

    // 댓글
    public static final String COMMENTS = API_BASE + "/comments";
    public static final String COMMENT_EDIT = COMMENTS + "/{commentId}/edit";
    public static final String COMMENT_DELETE = COMMENTS + "/{commentId}";
    public static final String COMMENTS_FOR_POST = COMMENTS + "/post/{postId}";

    // 좋아요
    public static final String LIKES = API_BASE + "/likes";
    public static final String LIKE_POST = LIKES + "/post/{postId}";
    public static final String LIKE_COMMENT = LIKES + "/comment/{commentId}";

    // 게시글
    public static final String POSTS = API_BASE + "/posts";
    public static final String POST_EDIT = POSTS + "/{postId}/edit";
    public static final String POST_DELETE = POSTS + "/{postId}";
    public static final String POST_DETAIL = POSTS + "/{postId}";
    public static final String BLOG_POSTS = POSTS + "/blog/{nickname}";
    public static final String SEARCH_POSTS = POSTS + "/search";

    // 신고
    public static final String REPORTS = API_BASE + "/reports";
    public static final String REPORT_USER = REPORTS + "/users";
    public static final String REPORT_POST = REPORTS + "/posts";
    public static final String REPORT_COMMENT = REPORTS + "/comments";

    // S3
    public static final String S3 = API_BASE + "/s3";
    public static final String PRESIGNED_URL = S3 + "/presigned-url";

    // 태그
    public static final String TAGS = API_BASE + "/tags";
    public static final String TAGS_FOR_BLOG = TAGS + "/blog/{nickname}";

}
