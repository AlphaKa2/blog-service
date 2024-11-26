package com.alphaka.blogservice.report.dto;

import com.alphaka.blogservice.report.entity.Reason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotNull(message = "신고 대상 ID를 입력해주세요.")
    private Long targetId;

    @NotNull(message = "신고 사유를 선택해주세요.")
    private Reason reason;

    private String details;
}
