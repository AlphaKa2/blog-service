package com.alphaka.blogservice.service;

import com.alphaka.blogservice.exception.ErrorCode;
import com.alphaka.blogservice.exception.custom.S3Exception;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 프로필 이미지 업로드 (upload 폴더)
    public String uploadUserProfileImage(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            log.error("파일이 비어있습니다.");
            throw new S3Exception(ErrorCode.S3_FILE_EMPTY);
        }
        return this.uploadFile(image, "profile", "image");
    }

    // 게시글 이미지 업로드 (upload 폴더)
    public String uploadPostImage(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            log.error("파일이 비어있습니다.");
            throw new S3Exception(ErrorCode.S3_FILE_EMPTY);
        }
        return this.uploadFile(image, "post_image", "image");
    }

    // 게시글 비디오 업로드 (upload 폴더)
    public String uploadPostVideo(MultipartFile video) {
        if (video.isEmpty() || Objects.isNull(video.getOriginalFilename())) {
            log.error("파일이 비어있습니다.");
            throw new S3Exception(ErrorCode.S3_FILE_EMPTY);
        }
        return this.uploadFile(video, "post_video", "video");
    }

    // 공통 파일 업로드 메서드 (upload 폴더에 저장)
    private String uploadFile(MultipartFile file, String prefix, String fileType) {
        this.validateFileSize(file, fileType);
        this.validateFileExtension(file, fileType);
        try {
            return this.uploadFileToS3(file, prefix, fileType);
        } catch (IOException e) {
            log.error("S3 파일 업로드 중 오류가 발생했습니다.", e);
            throw new S3Exception(ErrorCode.S3_FILE_UPLOAD_FAILED);
        }
    }

    // 파일 확장자 검증 (이미지 및 비디오)
    private void validateFileExtension(MultipartFile file, String fileType) {
        String filename = file.getOriginalFilename();
        if (Objects.isNull(filename)) {
            log.error("파일 확장자가 없습니다.");
            throw new S3Exception(ErrorCode.S3_FILE_EXTENSION_MISSING);
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        List<String> allowedExtensionList;

        // 이미지 및 비디오 확장자 검증
        if ("image".equals(fileType)) {
            allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif");
        } else if ("video".equals(fileType)) {
            allowedExtensionList = Arrays.asList("mp4", "avi", "mov", "wmv");
        } else {
            throw new S3Exception(ErrorCode.S3_FILE_EXTENSION_INVALID);
        }

        // 지원하지 않는 확장자인 경우 예외 처리
        if (!allowedExtensionList.contains(extension)) {
            log.error("지원하지 않는 파일 확장자입니다. [{}]", extension);
            throw new S3Exception(ErrorCode.S3_FILE_EXTENSION_INVALID);
        }
    }

    // 파일 크기 검증 (이미지 최대 10MB, 비디오 최대 100MB)
    private void validateFileSize(MultipartFile file, String fileType) {
        long maxSize;

        if ("image".equals(fileType)) {
            maxSize = 10 * 1024 * 1024;  // 10MB
        } else if ("video".equals(fileType)) {
            maxSize = 100 * 1024 * 1024;  // 100MB
        } else {
            log.error("지원되지 않는 파일 유형입니다.");
            throw new S3Exception(ErrorCode.S3_FILE_EXTENSION_INVALID);
        }

        if (file.getSize() > maxSize) {
            log.error("파일 크기가 너무 큽니다. [{} bytes, 허용된 최대 크기: {} bytes]", file.getSize(), maxSize);
            throw new S3Exception(ErrorCode.S3_FILE_SIZE_EXCEEDED);  // 커스텀 예외 처리
        }
    }

    // S3에 파일 업로드 처리 (upload 폴더에 저장)
    private String uploadFileToS3(MultipartFile file, String prefix, String fileType) throws IOException {
        String filename = file.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String s3FileName = "upload/" + prefix + "_" + UUID.randomUUID().toString().substring(0, 10) + extension; // 파일명 규칙 적용
        log.info("업로드 파일명: {}", s3FileName);

        InputStream is = file.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        log.info("파일 크기: {} KB", bytes.length / 1024);

        if ("image".equals(fileType)) {
            metadata.setContentType("image/" + extension.substring(1)); // 이미지 ContentType 설정
        } else if ("video".equals(fileType)) {
            metadata.setContentType("video/" + extension.substring(1)); // 비디오 ContentType 설정
        }
        log.info("ContentType: {}", metadata.getContentType());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest); // S3에 파일 업로드
        } catch (Exception e) {
            log.error("S3 파일 업로드 중 오류가 발생했습니다.", e);
            throw new S3Exception(ErrorCode.S3_OBJECT_UPLOAD_FAILED);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        log.info("파일 업로드 완료: {}", s3FileName);
        return amazonS3.getUrl(bucketName, s3FileName).toString(); // 업로드된 파일의 URL 반환
    }
}