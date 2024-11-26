package com.alphaka.blogservice.util;

import java.lang.reflect.Field;

public class TestUtil {
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정에 실패했습니다.", e);
        }
    }
}