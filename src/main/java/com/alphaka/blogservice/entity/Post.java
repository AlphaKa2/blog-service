package com.alphaka.blogservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends DeletableBaseEntity {

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
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isPublic = true; // 공개 여부 (기본값: 공개)

    @Column(nullable = false)
    private boolean isCommentable = true; // 댓글 허용 여부 (기본값: 공개)

    @Column(nullable = false)
    private int viewCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    // 포스트 삭제 시 연관된 댓글들도 삭제(soft delete)
    @PreRemove
    public void preRemove() {
        this.softDelete();
        for (Comment comment : comments) {
            comment.softDelete();
        }
        postTags.clear(); // 게시글 태그 물리적 삭제
        likes.clear(); // 좋아요 물리적 삭제
    }
}
