package com.revy.example.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 게시글 입력 폼 데이터를 담는 DTO.
 */
public class PostForm {

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

    /**
     * 기본 생성자.
     */
    public PostForm() {
    }

    /**
     * 제목을 반환한다.
     *
     * @return 제목
     */
    public String getTitle() {
        return title;
    }

    /**
     * 제목을 설정한다.
     *
     * @param title 제목
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 내용을 반환한다.
     *
     * @return 내용
     */
    public String getContent() {
        return content;
    }

    /**
     * 내용을 설정한다.
     *
     * @param content 내용
     */
    public void setContent(String content) {
        this.content = content;
    }
}
