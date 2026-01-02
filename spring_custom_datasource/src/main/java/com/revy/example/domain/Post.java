package com.revy.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 게시글 정보를 저장하는 엔티티.
 */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private long count = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA 기본 생성자.
     */
    protected Post() {
    }

    /**
     * 게시글을 생성한다.
     *
     * @param title 제목
     * @param content 내용
     */
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 게시글 ID를 반환한다.
     *
     * @return 게시글 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 게시글 제목을 반환한다.
     *
     * @return 게시글 제목
     */
    public String getTitle() {
        return title;
    }

    /**
     * 게시글 내용을 반환한다.
     *
     * @return 게시글 내용
     */
    public String getContent() {
        return content;
    }

    /**
     * 생성 시간을 반환한다.
     *
     * @return 생성 시간
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시간을 반환한다.
     *
     * @return 수정 시간
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 게시글 정보를 수정한다.
     *
     * @param title 제목
     * @param content 내용
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }



    /**
     * 생성 시각과 수정 시각을 초기화한다.
     */
    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 수정 시각을 갱신한다.
     */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
