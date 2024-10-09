package com.alphaka.blogservice.entity;

import jakarta.persistence.Column;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeletableBaseEntity extends BaseEntity {

    // null 값 존재 가능
    // 삭제 되면 삭제 시간 직접 기록
    @Column
    private LocalDateTime deletedAt;

    // 삭제 시간 기록
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
