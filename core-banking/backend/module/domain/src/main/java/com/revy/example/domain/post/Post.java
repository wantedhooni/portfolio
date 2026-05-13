package com.revy.example.domain.post;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PUBLIC)
public class Post extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Long viewCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    public static Post create(Long userId, String title, String summary, String content) {
        Post post = new Post();
        post.userId = userId;
        post.title = title;
        post.summary = summary;
        post.content = content;
        post.category = "GENERAL";
        post.status = "PUBLISHED";
        post.viewCount = 0L;
        post.publishedAt = Instant.now();
        return post;
    }
}
