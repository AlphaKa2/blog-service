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

    @Value("${cloud.aws.s3.bucketName}")
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

}