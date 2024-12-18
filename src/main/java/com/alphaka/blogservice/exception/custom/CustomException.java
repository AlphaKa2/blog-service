package com.alphaka.blogservice.exception.custom;

import lombok.Getter;

/**
 * 모든 커스텀 예외의 상위 클래스
 * 각 비즈니스 로직의 커스텀 예외는 이 클래스를 상속받아 구현
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 커스텀 예외 생성자
     * @param errorCode - 에러 코드 (ErrorCode Enum)
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
