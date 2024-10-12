package com.alphaka.blogservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends DeletableBaseEntity {

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

    // 부모 댓글 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false)
    private boolean isPublic = true; // 공개 여부 (기본값: 공개)

    // 자식 댓글 참조
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder
    public Comment(Long userId, Post post, String content, Comment parent, boolean isPublic) {
        this.userId = userId;
        this.post = post;
        this.content = content;
        this.parent = parent;
        this.isPublic = isPublic;
    }

    // 댓글 삭제 시 연관된 자식 댓글들도 삭제(soft delete)
    @PreRemove
    public void preRemove() {
        this.softDelete();
        for (Comment child : children) {
            child.softDelete();
        }
        likes.clear(); // 좋아요 물리적 삭제
    }
}
