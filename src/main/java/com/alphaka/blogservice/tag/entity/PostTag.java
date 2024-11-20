package com.alphaka.blogservice.tag.entity;

import com.alphaka.blogservice.common.entity.DeleteBaseEntity;
import com.alphaka.blogservice.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "post_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostTag extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
