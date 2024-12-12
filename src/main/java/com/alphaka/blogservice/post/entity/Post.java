package com.alphaka.blogservice.post.entity;

import com.alphaka.blogservice.blog.entity.Blog;
import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.common.entity.DeleteBaseEntity;
import com.alphaka.blogservice.like.entity.Like;
import com.alphaka.blogservice.report.entity.Report;
import com.alphaka.blogservice.tag.entity.PostTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob // HTML 형식의 큰 데이터 저장
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(nullable = false)
    private boolean isCommentable = true;

    @Column(nullable = false)
    private int viewCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @Builder
    public Post(Long userId, Blog blog, String title, String content, boolean isPublic, boolean isCommentable) {
        this.userId = userId;
        this.blog = blog;
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        this.isCommentable = isCommentable;
    }

    // 게시글 수정
    public void updatePost(String title, String content, boolean isPublic, boolean isCommentable) {
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        this.isCommentable = isCommentable;
    }
}
