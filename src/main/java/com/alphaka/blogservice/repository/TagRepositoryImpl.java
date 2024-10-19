package com.alphaka.blogservice.repository;

import com.alphaka.blogservice.entity.Tag;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

public class TagRepositoryImpl implements TagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void batchInsert(List<Tag> tags) {
        String sql = "INSERT INTO tags (name) VALUES (?)";

        jdbcTemplate.batchUpdate(sql, tags, tags.size(), (PreparedStatement ps, Tag tag) -> {
            ps.setString(1, tag.getTagName());
        });
    }
}
