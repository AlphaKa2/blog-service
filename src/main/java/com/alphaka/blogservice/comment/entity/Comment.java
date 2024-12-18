package com.alphaka.blogservice.comment.entity;

import com.alphaka.blogservice.common.entity.DeleteBaseEntity;
import com.alphaka.blogservice.like.entity.Like;
import com.alphaka.blogservice.post.entity.Post;
import com.alphaka.blogservice.report.entity.Report;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean isPublic = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @Builder
    public Comment(Long userId, Post post, String content, boolean isPublic, Comment parent) {
        this.userId = userId;
        this.post = post;
        this.content = content;
        this.isPublic = isPublic;
        this.parent = parent;
    }

    // 댓글 수정
    public void updateComment(String content, boolean isPublic) {
        this.content = content;
        this.isPublic = isPublic;
    }
}
