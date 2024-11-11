package com.alphaka.blogservice.dto.request;

import com.alphaka.blogservice.entity.Reason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportRequest {

    @NotNull(message = "신고 대상 ID를 입력해주세요.")
    private Long targetId;

    @NotNull(message = "신고 사유를 선택해주세요.")
    private Reason reason;

    private String details;
}
