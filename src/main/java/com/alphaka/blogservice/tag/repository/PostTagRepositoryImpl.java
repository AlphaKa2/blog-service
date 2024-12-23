package com.alphaka.blogservice.tag.repository;

import com.alphaka.blogservice.tag.entity.PostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostTagRepositoryImpl implements PostTagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsert(List<PostTag> postTags) {
        String sql = "INSERT INTO post_tags (post_id, tag_id, created_at, updated_at, deleted_at) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at), deleted_at = NULL";

        jdbcTemplate.batchUpdate(sql, postTags, postTags.size(), (PreparedStatement ps, PostTag postTag) -> {
            ps.setLong(1, postTag.getPost().getId());
            ps.setLong(2, postTag.getTag().getId());
            ps.setTimestamp(3, postTag.getCreatedAt() != null
                    ? Timestamp.valueOf(postTag.getCreatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, postTag.getUpdatedAt() != null
                    ? Timestamp.valueOf(postTag.getUpdatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        });
    }
}
