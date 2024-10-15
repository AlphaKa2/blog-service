package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.request.UserInfo;
import com.alphaka.blogservice.dto.request.UserProfile;
import com.alphaka.blogservice.dto.response.PostListResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Blog;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.projection.PostDetailProjectionImpl;
import com.alphaka.blogservice.projection.PostListProjection;
import com.alphaka.blogservice.repository.BlogRepository;
import com.alphaka.blogservice.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final S3Service s3Service;
    private final TagService tagService;
    private final UserClient userClient;
    private final UserProfileService userProfileService;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 특정 블로그의 게시글 목록 조회
     * @param request HTTP 요청
     * @param nickname 블로그 닉네임
     * @param pageable 페이징 정보
     * @return List<PostListResponse> 게시글 정보 목록
     */
    public Page<PostListResponse> getBlogPostList(HttpServletRequest request, String nickname, Pageable pageable) {
        log.info("블로그 게시글 목록 조회 요청 - Nickname: {}, Page: {}", nickname, pageable.getPageNumber());

        // 현재 요청한 사용자 조회 (null일 수 있음)
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(request);

        // 블로그 사용자 ID 조회
        UserInfo user = userClient.findUser(nickname).getData();
        // 블로그 조회
        Blog blog = blogRepository.findById(user.getUserId()).orElseThrow(BlogNotFoundException::new);

        boolean isOwner = false;

        // 현재 사용자가 블로그 소유자인지 확인
        if (currentUser != null) {
            isOwner = blog.getUserId().equals(currentUser.getUserId());
        }

        // 게시글 목록 조회
        Page<PostListProjection> postProjections = postRepository.findPostsByBlogId(user.getUserId(), isOwner, pageable);

        // Projection을 DTO로 변환
        Page<PostListResponse> responsePage = postProjections.map(projection -> PostListResponse.builder()
                .postId(projection.getPostId())
                .title(projection.getTitle())
                .contentSnippet(extractContentSnippet(projection.getContent()))
                .representativeImage(extractFirstImage(projection.getContent()))
                .likeCount(projection.getLikeCount())
                .commentCount(projection.getCommentCount())
                .viewCount(projection.getViewCount())
                .tags(projection.getTags())
                .createdAt(projection.getCreatedAt().toString())
                .build());

        log.info("블로그의 게시글 리스트 조회 완료 - Nickname: {}", nickname);
        return responsePage;
    }

    /**
     * 특정 게시글 상세 조회
     * @param postId 게시글 ID
     * @param httpRequest HTTP 요청
     * @return PostDetailResponse 게시글 상세 정보
     */
    public PostResponse getPostDetails(HttpServletRequest httpRequest, Long postId) {
        log.info("게시글 상세 조회 요청 - Post ID: {}", postId);

        // 게시글 조회
        PostDetailProjectionImpl projection = (PostDetailProjectionImpl) postRepository.findPostDetailById(postId);
        if (projection == null) {
            log.error("게시글을 찾을 수 없습니다 - Post ID: {}", postId);
            throw new PostNotFoundException();
        }

        // 태그 조회
        List<String> tags = postRepository.findTagsByPostId(postId);

        // 작성자 정보 확인
        UserInfo author = userClient.findUser(projection.getAuthorId()).getData();

        // 현재 사용자 정보 확인
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 비공개 게시글 여부 확인 및 처리
        if (!projection.getIsPublic() && !isAuthor(userProfile, author)) {
            // 비공개 게시글 처리 - 작성자가 아닌 경우
            log.info("비공개 게시글 - Post ID: {}", postId);
            return handlePrivatePost(projection, author, postId);
        }

        // 게시글 정보 매핑 및 응답 생성 (공개 게시글 또는 작성자가 맞는 경우)
        PostResponse response = PostResponse.builder()
                .postId(postId)
                .author(author.getNickname())
                .title(projection.getTitle())
                .content(projection.getContent())
                .likeCount(projection.getLikeCount())
                .viewCount(projection.getViewCount())
                .tags(tags)
                .createdAt(projection.getCreatedAt())
                .build();

        // 조회수 증가
        increaseViewCount(postId, httpRequest);

        log.info("게시글 상세 조회 완료 - Post ID: {}", postId);
        return response;
    }

    /**
     * 게시글 작성
     * @param httpRequest HTTP 요청
     * @param request 작성할 게시글 정보
     * @return Long 작성한 게시글 번호
     */
    @Transactional
    public Long createPost(HttpServletRequest httpRequest, PostCreateRequest request) {
        log.info("게시글 작성 요청 - Nickname: {}, Title: {}", request.getNickname(), request.getTitle());

        // 현재 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);
        log.debug("userProfile.userId 타입: {}", userProfile.getUserId().getClass());
        // 닉네임을 통해 블로그 조회
        Blog blog = blogRepository.findByUserId(userProfile.getUserId());
        if (blog == null) {
            log.error("블로그를 찾을 수 없습니다 - User ID: {}", userProfile.getUserId());
            throw new BlogNotFoundException();
        }

        // 게시글 내용 처리
        String processedContent = processFiles(request.getContent(), request.getImages(), request.getVideos());

        Post post = Post.builder()
                .userId(userProfile.getUserId())
                .blog(blog)
                .title(request.getTitle())
                .content(processedContent)
                .isPublic(request.isVisible())
                .isCommentable(request.isCommentable())
                .build();

        postRepository.save(post);

        // 태그 처리
        tagService.updateTagsForPost(post, request.getTagNames());

        log.info("게시글 작성 완료 - Post ID: {}", post.getId());
        return post.getId();
    }

    /**
     * 게시글 수정
     * @param httpRequest HTTP 요청
     * @param postId 게시글 ID
     * @param request 게시글 수정 정보
     * @return Long 수정한 게시글 번호
     */
    @Transactional
    public Long updatePost(HttpServletRequest httpRequest, Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 요청 - Post ID: {}", postId);

        // 현재 사용자 정보 추출
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);

        // 게시글 작성자 확인
        Post post = validatePostOwnership(postId, userProfile.getUserId());

        // 게시글 내용 처리
        String processedContent = processFiles(request.getContent(), request.getImages(), request.getVideos());

        post.updatePost(request.getTitle(), processedContent, request.isVisible(), request.isCommentable());

        postRepository.save(post);
        // 태그 업데이트
        tagService.updateTagsForPost(post, request.getTagNames());

        log.info("게시글 수정 완료 - Post ID: {}", post.getId());
        return post.getId();
    }

    /**
     * 게시글 삭제
     * @param request HTTP 요청
     * @param postId 게시글 ID
     */
    @Transactional
    public void deletePost(HttpServletRequest request, Long postId) {
        log.info("게시글 삭제 요청 - Post ID: {}", postId);

        UserProfile userProfile = userProfileService.getUserProfileFromHeader(request); // 사용자 정보 추출
        Post post = validatePostOwnership(postId, userProfile.getUserId());  // 게시글 작성자 확인

        postRepository.delete(post);
        log.info("게시글 삭제 완료 - Post ID: {}", post.getId());
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViewCount(Long postId, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String redisKey = "post:viewCount:" + postId + ":" + ipAddress; // Redis 키 구성
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // Redis에 조회수 증가 여부 확인
        Boolean isNewView = ops.setIfAbsent(redisKey, "1", 1, TimeUnit.DAYS);
        if (Boolean.TRUE.equals(isNewView)) {
            postRepository.increaseViewCount(postId);  // 조회수 증가
        }
    }

    /**
     * 조회한 비공개 게시글 처리
     * 작성자가 아닌 경우 게시글 내용을 비공개로 처리
     */
    @Transactional
    public PostResponse handlePrivatePost(PostDetailProjectionImpl projection, UserInfo author, Long postId) {
        return PostResponse.builder()
                .postId(postId)
                .author(author.getNickname())
                .title("비공개 게시글입니다.")
                .content("비공개 게시글입니다.")
                .likeCount(null)
                .viewCount(null)
                .tags(null)
                .createdAt(projection.getCreatedAt())
                .build();
    }

    /**
     * 게시글 작성자인지 확인
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return Post 객체
     */
    private Post validatePostOwnership(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // 게시글 작성자와 현재 사용자 ID가 같은지 확인
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
     * @return content 파일 경로가 반영된 HTML 내용
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
        return processMedia(content, images, "image");
    }

    // 비디오 파일 처리
    private String processVideos(String content, List<MultipartFile> videos) {
        return processMedia(content, videos, "video");
    }

    // 미디어 파일 처리
    private String processMedia(String content, List<MultipartFile> mediaFiles, String mediaType) {
        for (MultipartFile media : mediaFiles) {
            try {
                String mediaName = Objects.requireNonNull(media.getOriginalFilename());
                if (content.contains(mediaName)) {
                    String mediaUrl;
                    if (mediaType.equals("image")) {
                        mediaUrl = s3Service.uploadPostImage(media);
                    } else {
                        mediaUrl = s3Service.uploadPostVideo(media);
                    }
                    content = content.replace(mediaName, mediaUrl);  // HTML에서 파일명을 S3 URL로 교체
                }
            } catch (Exception e) {
                log.error("{} 처리 중 오류가 발생했습니다: {}", mediaType, media.getOriginalFilename(), e);
            }
        }
        return content;
    }

    /**
     * HTML 내용에서 첫 번째 이미지 URL 추출
     * @param content HTML 내용
     * @return 첫 이미지 URL
     */
    private String extractFirstImage(String content) {
        // Jsoup 라이브러리를 사용하여 HTML 파싱
        Document document = Jsoup.parse(content);

        // 첫 번째 이미지 태그 추출
        Element firstImage = document.selectFirst("img");

        // 이미지 태그가 존재하면 src 속성값 반환
        return (firstImage != null) ? firstImage.attr("src") : null;
    }

    /**
     * HTML 내용에서 처음 100자 추출
     * @param content HTML 내용
     * @return contentSnippet 내용 일부
     */
    private String extractContentSnippet(String content) {
        // HTML 파싱
        Document document = Jsoup.parse(content);

        // 이미지와 비디오 태그 제거
        document.select("img, video").remove();

        // 태그 제거 후 내용 추출
        String text = document.text().trim();

        // 50자 이상이면 50자까지만 반환
        return text.length() > 50 ? text.substring(0, 50).trim() + "..." : text;
    }

    /**
     * 클라이언트 IP 주소 추출
     * @param request HTTP 요청
     * @return IP 주소
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return (xForwardedFor != null) ? xForwardedFor.split(",")[0] : request.getRemoteAddr();
    }

    /**
     * 작성자 확인
     * @param userProfile 현재 로그인한 사용자
     * @param author 게시글 작성자 정보
     * @return 작성자가 일치하는지 여부
     */
    private boolean isAuthor(UserProfile userProfile, UserInfo author) {
        return userProfile != null && userProfile.getUserId().equals(author.getUserId());
    }
}