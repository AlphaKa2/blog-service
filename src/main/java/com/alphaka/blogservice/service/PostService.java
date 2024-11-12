package com.alphaka.blogservice.service;

import com.alphaka.blogservice.client.UserClient;
import com.alphaka.blogservice.common.dto.CurrentUser;
import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.dto.request.PostCreateRequest;
import com.alphaka.blogservice.dto.request.PostUpdateRequest;
import com.alphaka.blogservice.dto.response.PostListResponse;
import com.alphaka.blogservice.dto.response.PostResponse;
import com.alphaka.blogservice.entity.Blog;
import com.alphaka.blogservice.entity.Post;
import com.alphaka.blogservice.exception.custom.BlogNotFoundException;
import com.alphaka.blogservice.exception.custom.PostNotFoundException;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.repository.blog.BlogRepository;
import com.alphaka.blogservice.repository.post.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final CacheService cacheService;
    private final TagService tagService;
    private final UserClient userClient;
    private final BlogRepository blogRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 게시글 작성
     * @param currentUser - 현재 사용자 정보
     * @param request - 게시글 작성 정보
     * @return Long - 작성한 게시글 번호
     */
    @Transactional
    public Long createPost(CurrentUser currentUser, PostCreateRequest request) {
        log.info("게시글 작성 요청 - Nickname: {}, Title: {}", currentUser.getNickname(), request.getTitle());

        // 현재 사용자의 블로그 확인
        Blog blog = blogRepository.findByUserId(currentUser.getUserId())
                .orElseThrow(BlogNotFoundException::new);

        // 게시글 생성
        Post post = Post.builder()
                .userId(currentUser.getUserId())
                .blog(blog)
                .title(request.getTitle())
                .content(request.getContent())
                .isPublic(request.isPublic())
                .isCommentable(request.isCommentable())
                .build();
        postRepository.save(post);

        // 태그 연결
        if (!request.getTagNames().isEmpty()) {
            tagService.addTagsToPost(post, request.getTagNames());
        }

        log.info("게시글 작성 완료 - Post ID: {}", post.getId());

        // 게시글 작성 후, 블로그의 postList와 tagList 캐시 무효화
        Long blogId = blog.getId();
        cacheService.evictPostListAndTagListCache(blogId);

        return post.getId();
    }

    /**
     * 게시글 수정을 위한 데이터 조회
     * @param currentUser - 현재 사용자 정보
     * @param postId - 게시글 ID
     */
    public PostUpdateRequest getPostUpdateData(CurrentUser currentUser, Long postId) {
        log.info("게시글 수정 데이터 조회 요청 - Post ID: {}", postId);

        // 게시글 존재와 작성자 여부 확인
        Post post = validatePostOwnership(postId, currentUser.getUserId());

        // 게시글 수정 데이터 조회
        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .isPublic(post.isPublic())
                .isCommentable(post.isCommentable())
                .tagNames(tagService.findTagsByPostId(postId))
                .build();

        log.info("게시글 수정 데이터 조회 완료 - Post ID: {}", postId);
        return postUpdateRequest;
    }

    /**
     * 게시글 수정
     * @param currentUser - 현재 사용자 정보
     * @param postId - 게시글 ID
     * @param request - 게시글 수정 정보
     * @return Long - 수정한 게시글 번호
     */
    @Transactional
    public Long updatePost(CurrentUser currentUser, Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 요청 - Post ID: {}", postId);

        // 게시글 존재와 작성자 확인
        Post post = validatePostOwnership(postId, currentUser.getUserId());

        // 게시글 업데이트
        post.updatePost(request.getTitle(), request.getContent(), request.isPublic(), request.isCommentable());
        postRepository.save(post);

        // 태그 업데이트
        tagService.updateTagsForPost(post, request.getTagNames());

        log.info("게시글 수정 완료 - Post ID: {}", post.getId());

        // 게시글 수정 후, 블로그의 postList와 tagList 캐시 무효화 및 해당 게시글의 postDetails 캐시 무효화
        Long blogId = post.getBlog().getId();
        cacheService.evictPostListAndTagListCache(blogId);
        cacheService.evictPostDetailsCache(postId);

        return post.getId();
    }

    /**
     * 게시글 삭제
     * @param currentUser - HTTP 요청
     * @param postId - 게시글 ID
     */
    @Transactional
    public void deletePost(CurrentUser currentUser, Long postId) {
        log.info("게시글 삭제 요청 - Post ID: {}", postId);

        Post post = validatePostOwnership(postId, currentUser.getUserId());  // 게시글 작성자 확인

        postRepository.delete(post);
        log.info("게시글 삭제 완료 - Post ID: {}", post.getId());

        // 게시글 삭제 후, 블로그의 postList와 tagList 캐시 무효화 및 해당 게시글의 postDetails 캐시 무효화
        Long blogId = post.getBlog().getId();
        cacheService.evictCommentsCache(postId);
        cacheService.evictPostListAndTagListCache(blogId);
        cacheService.evictPostDetailsCache(postId);
    }

    /**
     * 특정 게시글 상세 조회
     * @param request - HTTP 요청
     * @param currentUser - 현재 사용자 정보
     * @param postId - 게시글 ID
     * @return PostDetailResponse - 게시글 상세 정보
     */
    @Transactional
    @Cacheable(value = "blogService:postDetails", key = "#postId", unless = "#result == null")
    public PostResponse getPostResponse(HttpServletRequest request, CurrentUser currentUser, Long postId) {
        log.info("게시글 상세 조회 요청 - Post ID: {}", postId);

        // 게시글이 비공개인지 확인 후 처리
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        if (!post.isPublic() && !currentUser.getUserId().equals(post.getUserId())) {
            throw new UnauthorizedException();
        }

        // 게시글 상세 정보 조회
        PostResponse postResponse = postRepository.getPostResponse(postId, currentUser.getUserId())
                .orElseThrow(PostNotFoundException::new);

        // 게시글 작성자 입력
        postResponse.setAuthor(userClient.findUserById(postResponse.getAuthorId()).getData().getNickname());

        // 게시글 태그 입력
        List<String> tags = tagService.findTagsByPostId(postId);
        postResponse.setTags(tags);

        // 조회수 증가
        increaseViewCount(postId, request);

        log.info("게시글 상세 조회 완료 - Post ID: {}", postId);
        return postResponse;
    }

    /**
     * 특정 블로그의 게시글 목록 조회 (페이징, 정렬 default: 최신순)
     * latest: 최신순, oldest: 오래된순, views: 조회수 많은순, likes: 좋아요 많은순
     * @param currentUser - 현재 사용자 정보
     * @param nickname - 블로그 주인 닉네임
     */
    @Cacheable(value = "blogService:postList",
            key = "@postService.getBlogIdByNickname(#nickname) + '-' + #pageable.pageNumber + " +
                    "'-' + #pageable.pageSize + '-' + #pageable.sort.toString()",
            unless = "#result == null || #result.isEmpty()")
    public List<PostListResponse> getPostListResponse(CurrentUser currentUser, String nickname, Pageable pageable) {
        log.info("블로그 게시글 목록 조회 요청 - Nickname: {}", nickname);

        // 블로그 존재 여부 확인
        log.info("블로그 존재 여부 확인 - Nickname: {}", nickname);
        UserDTO user = userClient.findUserByNickname(nickname).getData();
        Blog blog = blogRepository.findByUserId(user.getUserId()).orElseThrow(BlogNotFoundException::new);

        // 현재 사용자가 블로그 주인인지 확인
        boolean isOwner = currentUser != null && currentUser.getUserId().equals(blog.getUserId());
        log.info("현재 사용자가 블로그 주인인지 확안: {}" , isOwner);

        // 게시글 목록 조회
        log.info("게시글 목록 조회");
        List<PostListResponse> postListResponses = postRepository.getPostListResponse(blog.getId(), isOwner, pageable);

        // 게시글 ID 목록 추출
        List<Long> postIds = postListResponses.stream()
                .map(PostListResponse::getPostId)
                .collect(Collectors.toList());

        // 게시글 내용에서 대표 이미지와 요약 추출
        for (PostListResponse postResponse : postListResponses) {
            // 대표 이미지 추출
            String representativeImage = extractFirstImage(postResponse.getContentSnippet());
            postResponse.setRepresentativeImage(representativeImage);

            // 내용 요약 추출
            String contentSnippet = extractContentSnippet(postResponse.getContentSnippet());
            postResponse.setContentSnippet(contentSnippet);
        }

        // 태그를 한 번에 조회하여 매핑
        Map<Long, List<String>> postTagsMap = tagService.findTagsByPostIds(postIds);

        for (PostListResponse postResponse : postListResponses) {
            List<String> tags = postTagsMap.get(postResponse.getPostId());
            postResponse.setTags(tags != null ? tags : new ArrayList<>());
        }

        log.info("블로그 게시글 목록 조회 완료 - Nickname: {}", nickname);
        return postListResponses;
    }

    /**
     * 조회수 증가
     * @param postId - 게시글 ID
     * @param httpRequest - HTTP 요청
     */
    @Transactional
    public void increaseViewCount(Long postId, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String redisKey = "post:viewCount:" + postId + ":" + ipAddress; // Redis 키 구성
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        // Redis에 조회수 증가 여부 확인
        Boolean isNewView = ops.setIfAbsent(redisKey, "1", 1, TimeUnit.DAYS);
        if (Boolean.TRUE.equals(isNewView)) {
            postRepository.increaseViewCount(postId);  // 조회수 증가
        }
    }

    /**
     * 게시글 소유권 확인
     * @param postId - 게시글 ID
     * @param userId - 사용자 ID
     * @return Post - 게시글
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
     * 클라이언트 IP 주소 추출
     * @param request - HTTP 요청
     * @return - IP 주소
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return (xForwardedFor != null) ? xForwardedFor.split(",")[0] : request.getRemoteAddr();
    }

    /**
     * HTML 내용에서 첫 번째 이미지 URL 추출
     * @param content - HTML 내용
     * @return String - 첫 이미지 URL
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
     * @param content - HTML 내용
     * @return String - contentSnippet 내용 일부
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
     * 닉네임을 통해 블로그 ID를 조회하는 메서드
     * @param nickname - 블로그 주인의 닉네임
     * @return 블로그 ID
     */
    public Long getBlogIdByNickname(String nickname) {
        // 닉네임을 통해 사용자 정보를 조회
        UserDTO user = userClient.findUserByNickname(nickname).getData();
        // 사용자 ID를 통해 블로그 정보를 조회
        Blog blog = blogRepository.findByUserId(user.getUserId())
                .orElseThrow(BlogNotFoundException::new);
        return blog.getId();
    }
}