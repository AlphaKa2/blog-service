package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.PostTag;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

public class PostTagRepositoryImpl implements PostTagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    public PostTagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void batchInsert(List<PostTag> postTags) {
        String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, postTags, postTags.size(), (PreparedStatement ps, PostTag postTag) -> {
            ps.setLong(1, postTag.getPost().getId());
            ps.setLong(2, postTag.getTag().getId());
        });
    }
}
