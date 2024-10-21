package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.QPostTag;
import com.alphaka.blogservice.entity.QTag;
import com.alphaka.blogservice.entity.Tag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

public class TagRepositoryImpl implements TagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory queryFactory;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate, JPAQueryFactory queryFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryFactory = queryFactory;
    }

    // 태그 배치 삽입
    @Override
    public void batchInsert(List<Tag> tags) {
        String sql = "INSERT INTO tags (name) VALUES (?)";

        jdbcTemplate.batchUpdate(sql, tags, tags.size(), (PreparedStatement ps, Tag tag) -> {
            ps.setString(1, tag.getTagName());
        });
    }

    // 게시글 ID로 태그 조회
    @Override
    public List<String> findTagsByPostId(Long postId) {
        QTag tag = QTag.tag;
        QPostTag postTag = QPostTag.postTag;

        return queryFactory
                .select(tag.tagName)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch();
    }
}
