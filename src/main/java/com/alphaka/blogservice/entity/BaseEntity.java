package com.alphaka.blogservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    //생성 시간과 업데이트 시간을 알려주는 부모 클래스

    // 특징:
    // 따로 테이블을 생성하지 않음
    // 대신 이 클래스의 필드들은 하위 엔티티에 포함됨

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // null 값 존재 가능
    // update 쿼리 전송될 때 생성
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    // null 값 존재 가능
    // 삭제 되면 삭제 시간 직접 기록
    private LocalDateTime deletedAt;

    // 논리적인 삭제 구현
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
