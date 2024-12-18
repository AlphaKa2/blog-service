package com.alphaka.blogservice.tag.entity;

import com.alphaka.blogservice.common.entity.CreateBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "tagName"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Tag extends CreateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String tagName;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @Builder
    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
