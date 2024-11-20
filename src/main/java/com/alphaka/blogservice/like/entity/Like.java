package com.alphaka.blogservice.like.entity;

import com.alphaka.blogservice.comment.entity.Comment;
import com.alphaka.blogservice.common.entity.CreateBaseEntity;
import com.alphaka.blogservice.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Like extends CreateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
