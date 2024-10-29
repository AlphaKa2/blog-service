package com.alphaka.blogservice.repository.tag;

import com.alphaka.blogservice.entity.PostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class PostTagRepositoryImpl implements PostTagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsert(List<PostTag> postTags) {
        String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, postTags, postTags.size(), (PreparedStatement ps, PostTag postTag) -> {
            ps.setLong(1, postTag.getPost().getId());
            ps.setLong(2, postTag.getTag().getId());
        });
    }
}
