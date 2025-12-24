package com.revy.example.service;


import com.revy.example.api.dto.PostCreateRequest;
import com.revy.example.api.dto.PostResponse;
import com.revy.example.api.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface PostService {
    PostResponse create(PostCreateRequest req);

    @Transactional(readOnly = true)
    PostResponse get(Long id);

    @Transactional(readOnly = true)
    Page<PostResponse> list(int page, int size);

    PostResponse update(Long id, PostUpdateRequest req);

    void delete(Long id);
}
