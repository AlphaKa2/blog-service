package com.alphaka.blogservice.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.bucket}")
    private String bucketName;

    private AmazonS3 amazonS3;

    // 서명된 URL을 생성하는 메서드
    public String generatePresignedUrl(String fileName, String fileType) {

        // 만료 시간 (5분)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5;
        expiration.setTime(expTimeMillis);

        // 서명된 URL 생성
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        // 파일 타입 설정
        request.setContentType(fileType);

        // 서명된 URL 생성
        URL url = amazonS3.generatePresignedUrl(request);

        return url.toString();
    }
}
