package com.alphaka.blogservice.common.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class DeleteBaseEntity extends UpdateBaseEntity {

    private LocalDateTime deletedAt;

    /* 논리적 삭제 */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
