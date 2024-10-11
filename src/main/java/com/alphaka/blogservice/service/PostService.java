package com.alphaka.blogservice.service;

import com.alphaka.blogservice.Mapper.PostMapper;
import com.alphaka.blogservice.client.AuthClient;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.repository.BlogRepository;
import com.alphaka.blogservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final S3Service s3Service;
    private final AuthClient authClient;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper = PostMapper.INSTANCE;

    /**
     * 게시글 작성
     * @param token JWT 토큰
     * @param request PostCreateRequest
     * @return PostResponse
     */
    @Transactional
    public PostResponse createPost(String token, PostCreateRequest request) {
        log.info("게시글 작성 요청 - Blog ID: {}, Title: {}", request.getBlogId(), request.getTitle());

        Long userId = getAuthenticatedUserId(token);  // 사용자 ID 추출 및 확인

        String processedContent = processFiles(request.getContent(), request.getImages(), request.getVideos());

        Post post = Post.builder()
                .userId(userId)
                .blog(blogRepository.findById(request.getBlogId()).orElseThrow(BlogNotFoundException::new))
                .title(request.getTitle())
                .content(processedContent)
                .isPublic(!request.isPrivate())
                .isCommentable(request.isCommentable())
                .build();

        postRepository.save(post);
        log.info("게시글 작성 완료 - Post ID: {}", post.getId());

        return postMapper.toResponse(post);
    }

    /**
     * HTML 내용에서 파일을 처리하고 경로를 업데이트
     * @param content HTML 내용
     * @param images 이미지 파일 목록
     * @param videos 비디오 파일 목록
     * @return 파일 경로가 반영된 HTML 내용
     */
    @Transactional
    public String processFiles(String content, List<MultipartFile> images, List<MultipartFile> videos) {
        log.info("게시글 미디어 파일 처리 시작");

        content = processImages(content, images);
        content = processVideos(content, videos);

        log.info("게시글 미디어 파일 처리 완료");
        return content;
    }

    // 이미지 파일 처리
    private String processImages(String content, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            String imageName = Objects.requireNonNull(image.getOriginalFilename());
            if (content.contains(imageName)) {
                String imageUrl = s3Service.uploadPostImage(image);
                content = content.replace(imageName, imageUrl);  // HTML에서 이미지 파일명을 S3 URL로 교체
            }
        }
        return content;
    }

    // 비디오 파일 처리
    private String processVideos(String content, List<MultipartFile> videos) {
        for (MultipartFile video : videos) {
            String videoName = Objects.requireNonNull(video.getOriginalFilename());
            if (content.contains(videoName)) {
                String videoUrl = s3Service.uploadPostVideo(video);
                content = content.replace(videoName, videoUrl);  // HTML에서 비디오 파일명을 S3 URL로 교체
            }
        }
        return content;
    }
}