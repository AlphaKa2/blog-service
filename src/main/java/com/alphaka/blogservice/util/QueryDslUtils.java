package com.alphaka.blogservice.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QueryDslUtils {

    public static List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable, String entityAlias) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                switch (order.getProperty()) {
                    case "createdAt" -> {
                        DateTimePath<LocalDateTime> createdAtPath = Expressions.dateTimePath(LocalDateTime.class, entityAlias + ".createdAt");
                        orders.add(new OrderSpecifier<>(direction, createdAtPath));
                    }
                    case "viewCount" -> {
                        NumberPath<Integer> viewCountPath = Expressions.numberPath(Integer.class, entityAlias + ".viewCount");
                        orders.add(new OrderSpecifier<>(direction, viewCountPath));
                    }
                    default -> throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다: " + order.getProperty());
                }
            }
        }

        return orders;
    }
}
