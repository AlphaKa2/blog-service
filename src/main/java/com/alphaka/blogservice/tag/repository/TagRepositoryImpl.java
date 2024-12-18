package com.alphaka.blogservice.tag.repository;

import com.alphaka.blogservice.tag.entity.QPostTag;
import com.alphaka.blogservice.tag.entity.QTag;
import com.alphaka.blogservice.tag.entity.Tag;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory queryFactory;

    // 태그 배치 삽입
    @Override
    public void batchInsert(List<Tag> tags) {
        String sql = "INSERT INTO tags (tag_name, created_at) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, tags, tags.size(), (PreparedStatement ps, Tag tag) -> {
            ps.setString(1, tag.getTagName());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        });
    }

    // 게시글 ID 리스트별 태그 조회
    @Override
    public Map<Long, List<String>> findTagsByPostIds(List<Long> postIds) {
        QPostTag postTag = QPostTag.postTag;
        QTag tag = QTag.tag;

        List<Tuple> results = queryFactory
                .select(postTag.post.id, tag.tagName)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.in(postIds))
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(postTag.post.id),
                        Collectors.mapping(tuple -> tuple.get(tag.tagName), Collectors.toList())
                ));
    }
}
