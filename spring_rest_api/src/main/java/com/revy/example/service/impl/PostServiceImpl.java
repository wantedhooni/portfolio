package com.revy.example.service.impl;

import com.revy.example.api.dto.PostCreateRequest;
import com.revy.example.api.dto.PostResponse;
import com.revy.example.api.dto.PostUpdateRequest;
import com.revy.example.domain.Post;
import com.revy.example.domain.PostRepository;
import com.revy.example.exception.NotFoundException;
import com.revy.example.service.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    final private PostRepository postRepository;

    @Override
    public PostResponse create(PostCreateRequest req) {
        Post post = Post.builder()
                .title(req.title())
                .content(req.content())
                .build();
        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found. id=" + id));
        return toResponse(post);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PostResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return postRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public PostResponse update(Long id, PostUpdateRequest req) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found. id=" + id));

        post.setTitle(req.title());
        post.setContent(req.content());
        return toResponse(post); // dirty checking
    }

    @Override
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Post not found. id=" + id);
        }
        postRepository.deleteById(id);
    }

    private PostResponse toResponse(Post p) {
        return new PostResponse(p.getId(), p.getTitle(), p.getContent(), p.getCreatedDate(), p.getLastModifiedDate());
    }
}
