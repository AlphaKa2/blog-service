package com.alphaka.blogservice.controller;

import com.alphaka.blogservice.dto.response.ApiResponse;
import com.alphaka.blogservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private S3Service s3Service;

    // 서명된 URL 요청
    @PostMapping("/presigned-url")
    public ApiResponse<Map<String, String>> getPresignedUrl(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        String contentType = request.get("contentType");

        // 서명된 URL 생성
        String presignedUrl = s3Service.generatePresignedUrl(fileName, contentType);

        // 응답 데이터를 Map으로 작성
        Map<String, String> response = Map.of("url", presignedUrl);

        // ApiResponse로 감싸서 반환
        return new ApiResponse<>(response);
    }
}
