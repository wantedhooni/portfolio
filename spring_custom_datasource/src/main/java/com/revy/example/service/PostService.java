package com.revy.example.service;

import com.revy.example.domain.Post;
import com.revy.example.repository.PostRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 CRUD 비즈니스 로직을 처리하는 서비스.
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    /**
     * 서비스 생성자.
     *
     * @param postRepository 게시글 리포지토리
     */
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 게시글을 생성한다.
     *
     * @param title 제목
     * @param content 내용
     * @return 저장된 게시글
     */
    public Post create(String title, String content) {
        Post post = new Post(title, content);
        return postRepository.save(post);
    }

    /**
     * 게시글 단건을 조회한다.
     *
     * @param id 게시글 ID
     * @return 게시글
     */
    @Transactional(readOnly = true)
    public Post get(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    /**
     * 게시글 목록을 조회한다.
     *
     * @return 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> getAll() {
        return postRepository.findAll();
    }

    /**
     * 게시글을 수정한다.
     *
     * @param id 게시글 ID
     * @param title 제목
     * @param content 내용
     * @return 수정된 게시글
     */
    public Post update(Long id, String title, String content) {
        Post post = get(id);
        post.update(title, content);
        return post;
    }

    /**
     * 게시글을 삭제한다.
     *
     * @param id 게시글 ID
     */
    public void delete(Long id) {
        Post post = get(id);
        postRepository.delete(post);
    }
}
