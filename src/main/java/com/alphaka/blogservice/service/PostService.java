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
import com.alphaka.blogservice.repository.LikeRepository;
import com.alphaka.blogservice.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final TagService tagService;
    private final UserClient userClient;
    private final UserProfileService userProfileService;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
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

        // 현재 요청한 사용자와 블로그 소유자 확인
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(request);
        UserInfo blogOwner = userClient.findUser(nickname).getData();
        Blog blog = blogRepository.findByUserId(blogOwner.getUserId());
        if (blog == null) {
            log.error("블로그를 찾을 수 없습니다 - Nickname: {}", nickname);
            throw new BlogNotFoundException();
        }

        // 소유자 여부 확인
        boolean isOwner = isAuthor(currentUser, blogOwner);

        // 게시글 목록 조회 및 응답 매핑
        Page<PostListProjection> postListProjections = postRepository.findPostsByBlogId(blog.getId(), blogOwner.getUserId(), isOwner, pageable);
        // PostListProjection을 PostListResponse로 변환
        List<PostListResponse> postListResponses = postListProjections.getContent().stream()
                .map(postProjection -> {
                    List<String> tags = postRepository.findTagsByPostId(postProjection.getPostId());
                    return mapToPostListResponse(postProjection, tags);
                })
                .toList();

        log.info("블로그의 게시글 리스트 조회 완료 - Nickname: {}", nickname);
        return new PageImpl<>(postListResponses, pageable, postListProjections.getTotalElements());
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

        // 작성자와 현재 사용자 정보 조회
        UserInfo author = userClient.findUser(projection.getAuthorId()).getData();
        UserProfile currentUser = userProfileService.getUserProfileFromHeader(httpRequest);

        // 비공개 게시글 여부 확인 및 처리
        if (!projection.getIsPublic() && !isAuthor(currentUser, author)) {
            return handlePrivatePost(projection, author, postId);
        }

        // 태그 조회
        List<String> tags = postRepository.findTagsByPostId(postId);

        // 요청한 사용자 좋아요 여부 확인
        boolean isLiked = false;
        if (currentUser != null) {
            isLiked = likeRepository.existsByUserIdAndPost(currentUser.getUserId(),
                    postRepository.findById(postId).orElseThrow(PostNotFoundException::new));
        }
        PostResponse response = mapToPostResponse(projection, author, tags, isLiked);

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

        // 현재 사용자 확인 및 블로그 조회
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);
        Blog blog = blogRepository.findByUserId(userProfile.getUserId());
        if (blog == null) {
            log.error("블로그를 찾을 수 없습니다 - User ID: {}", userProfile.getUserId());
            throw new BlogNotFoundException();
        }

        Post post = Post.builder()
                .userId(userProfile.getUserId())
                .blog(blog)
                .title(request.getTitle())
                .content(request.getContent())
                .isPublic(request.isVisible())
                .isCommentable(request.isCommentable())
                .build();

        postRepository.save(post);

        // 태그 연결
        tagService.addTagsToNewPost(post, request.getTagNames());

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

        // 게시글 작성자 확인 및 업데이트
        UserProfile userProfile = userProfileService.getUserProfileFromHeader(httpRequest);
        Post post = validatePostOwnership(postId, userProfile.getUserId());

        // 게시글 업데이트
        post.updatePost(request.getTitle(), request.getContent(), request.isVisible(), request.isCommentable());
        postRepository.save(post);

        // 태그 업데이트
        tagService.updateTagsForExistingPost(post, request.getTagNames());

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
     * @param postId 게시글 ID
     * @param httpRequest HTTP 요청
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
     * @param projection 게시글 Projection
     * @param author 작성자 정보
     * @param postId 게시글 ID
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
     * 게시글 소유권 확인
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
     * 작성자 확인
     * @param currentUser 현재 로그인한 사용자
     * @param author 게시글 작성자 정보
     * @return 작성자가 일치하는지 여부
     */
    private boolean isAuthor(UserProfile currentUser, UserInfo author) {
        return currentUser != null && currentUser.getUserId().equals(author.getUserId());
    }

    /**
     * 게시글 목록 응답으로 변환
     * @param projection 게시글 Projection
     */
    private PostListResponse mapToPostListResponse(PostListProjection projection, List<String> tags) {
        return PostListResponse.builder()
                .postId(projection.getPostId())
                .title(projection.getTitle())
                .contentSnippet(extractContentSnippet(projection.getContent()))
                .representativeImage(extractFirstImage(projection.getContent()))
                .likeCount(projection.getLikeCount().intValue())  // int로 변환
                .commentCount(projection.getCommentCount().intValue())  // int로 변환
                .viewCount(projection.getViewCount())
                .tags(tags)  // 태그 리스트 설정
                .createdAt(projection.getCreatedAt().toString())  // 날짜 형식 맞춤
                .build();
    }

    /**
     * 게시글 상세 조회 응답으로 변환
     * @param projection 게시글 Projection
     * @param author 작성자 정보
     * @param tags 태그 목록
     */
    private PostResponse mapToPostResponse(PostDetailProjectionImpl projection, UserInfo author, List<String> tags, boolean isLiked) {
        return PostResponse.builder()
                .postId(projection.getPostId())
                .author(author.getNickname())
                .title(projection.getTitle())
                .content(projection.getContent())
                .likeCount(projection.getLikeCount())
                .viewCount(projection.getViewCount())
                .tags(tags)
                .isLike(isLiked)
                .createdAt(projection.getCreatedAt())
                .build();
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
}