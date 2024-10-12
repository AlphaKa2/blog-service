package com.alphaka.blogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponse(T data) {
        this.status = HttpStatus.OK.value();
        this.message = "요청이 성공적으로 처리되었습니다";
        this.data = data;
    }
}
