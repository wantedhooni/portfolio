package com.revy.example.repository;

import com.revy.example.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게시글 엔티티를 관리하는 리포지토리.
 */
public interface PostRepository extends JpaRepository<Post, Long> {
}
