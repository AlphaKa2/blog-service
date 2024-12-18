package com.alphaka.blogservice.exception.handler;

import com.alphaka.blogservice.common.response.ErrorResponse;
import com.alphaka.blogservice.exception.custom.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 * 모든 컨트롤러 및 서비스에서 발생하는 예외를 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 모든 커스텀 예외 처리
     * @param e - CustomException
     * @return ErrorResponse - 예외 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getErrorCode().getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode().getStatus(),
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(e.getErrorCode().getStatus()));
    }

//    /**
//     * Kafka 예외 처리
//     * @param e - KafkaException
//     * @return ErrorResponse - 예외 응답
//     */
//    @ExceptionHandler(KafkaException.class)
//    public ResponseEntity<ErrorResponse> handleKafkaException(KafkaException e) {
//        log.error("KafkaException 발생: {}", e.getMessage());
//        ErrorResponse errorResponse = new ErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "KAFKA_ERROR",
//                "Kafka 메시지 처리 중 오류가 발생했습니다."
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
