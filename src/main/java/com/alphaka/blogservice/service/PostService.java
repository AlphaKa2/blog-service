package com.alphaka.blogservice.service;

import com.alphaka.blogservice.Mapper.PostMapper;
import com.alphaka.blogservice.client.AuthClient;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.repository.BlogRepository;
import com.alphaka.blogservice.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final TagService tagService;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper = PostMapper.INSTANCE;

    /**
     * 게시글 작성
     * @param httpRequest HttpServletRequest
     * @param request PostCreateRequest
     * @return PostResponse
     */
    @Transactional
    public PostResponse createPost(HttpServletRequest httpRequest, PostCreateRequest request) {
        log.info("게시글 작성 요청 - Blog ID: {}, Title: {}", request.getBlogId(), request.getTitle());

        Long userId = getAuthenticatedUserId(httpRequest);  // 사용자 ID 추출 및 확인

        String processedContent = processFiles(request.getContent(), request.getImages(), request.getVideos());

        Post post = Post.builder()
                .userId(userId)
                .blog(blogRepository.findById(request.getBlogId()).orElseThrow(BlogNotFoundException::new))
                .title(request.getTitle())
                .content(processedContent)
                .isPublic(request.isPublic())
                .isCommentable(request.isCommentable())
                .build();

        postRepository.save(post);
        log.info("게시글 작성 완료 - Post ID: {}", post.getId());

        // 태그 업데이트
        tagService.updateTagsForPost(post, request.getTagNames());

        return postMapper.toResponse(post);
    }

    /**
     * 게시글 수정
     * @param httpRequest HttpServletRequest
     * @param postId 게시글 ID
     * @param request PostUpdateRequest
     * @return PostResponse
     */
    @Transactional
    public PostResponse updatePost(HttpServletRequest httpRequest, Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 요청 - Post ID: {}", postId);

        Long userId = getAuthenticatedUserId(httpRequest);
        Post post = validatePostOwnership(postId, userId);  // 게시글 작성자 확인

        String processedContent = processFiles(request.getContent(), request.getImages(), request.getVideos());

        post.updatePost(request.getTitle(), processedContent, request.isPublic(), request.isCommentable());
        postRepository.save(post);
        log.info("게시글 수정 완료 - Post ID: {}", post.getId());

        // 태그 업데이트
        tagService.updateTagsForPost(post, request.getTagNames());

        return postMapper.toResponse(post);
    }

    /**
     * 게시글 삭제
     * @param request HttpServletRequest
     * @param postId 게시글 ID
     */
    @Transactional
    public void deletePost(HttpServletRequest request, Long postId) {
        log.info("게시글 삭제 요청 - Post ID: {}", postId);

        Long userId = getAuthenticatedUserId(request);
        Post post = validatePostOwnership(postId, userId);  // 게시글 작성자 확인

        postRepository.delete(post);
        log.info("게시글 삭제 완료 - Post ID: {}", post.getId());
    }


    /**
     * 현재 인증된 사용자 ID를 추출하고 확인
     * @param request HttpServletRequest
     * @return 사용자 ID
     */
    private Long getAuthenticatedUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-USER-ID");

        if (userIdHeader == null) {
            log.error("헤더에서 사용자 정보를 찾을 수 없습니다.");
            throw new UnauthorizedException();
        }

        // 사용자 ID가 숫자인지 확인
        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("인증된 사용자 ID: {}", userId);
            return userId;
        } catch (NumberFormatException e) {
            log.error("헤더의 사용자 ID가 유효하지 않습니다: {}", userIdHeader);
            throw new UnauthorizedException();
        }
    }

    /**
     * 게시글 작성자인지 확인
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return Post 객체
     */
    private Post validatePostOwnership(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (!post.getUserId().equals(userId)) {
            log.error("게시글 작성자가 아닙니다 - Post ID: {}, User ID: {}", post.getId(), userId);
            throw new UnauthorizedException();
        }

        return post;
    }

    /**
     * HTML 내용에서 파일을 처리하고 경로를 업데이트
     * @param content HTML 내용
     * @param images 이미지 파일 목록
     * @param videos 비디오 파일 목록
     * @return 파일 경로가 반영된 HTML 내용
     */
    public String processFiles(String content, List<MultipartFile> images, List<MultipartFile> videos) {
        log.info("게시글 미디어 파일 처리 시작");

        content = processImages(content, images != null ? images : List.of());
        content = processVideos(content, videos != null ? videos : List.of());

        log.info("게시글 미디어 파일 처리 완료");
        return content;
    }

    // 이미지 파일 처리
    private String processImages(String content, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            try {
                String imageName = Objects.requireNonNull(image.getOriginalFilename());
                if (content.contains(imageName)) {
                    String imageUrl = s3Service.uploadPostImage(image);
                    content = content.replace(imageName, imageUrl);  // HTML에서 이미지 파일명을 S3 URL로 교체
                }
            } catch (Exception e) {
                log.error("이미지 처리 중 오류가 발생했습니다: {}", image.getOriginalFilename(), e);
            }
        }
        return content;
    }

    // 비디오 파일 처리
    private String processVideos(String content, List<MultipartFile> videos) {
        for (MultipartFile video : videos) {
            try {
                String videoName = Objects.requireNonNull(video.getOriginalFilename());
                if (content.contains(videoName)) {
                    String videoUrl = s3Service.uploadPostVideo(video);
                    content = content.replace(videoName, videoUrl);  // HTML에서 비디오 파일명을 S3 URL로 교체
                }
            } catch (Exception e) {
                log.error("비디오 처리 중 오류가 발생했습니다: {}", video.getOriginalFilename(), e);
            }
        }
        return content;
    }
}