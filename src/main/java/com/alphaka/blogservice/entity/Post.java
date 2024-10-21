package com.alphaka.blogservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

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

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

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
