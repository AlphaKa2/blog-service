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
}