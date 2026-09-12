package com.revy.example.controller;

import com.revy.example.domain.Post;
import com.revy.example.dto.PostForm;
import com.revy.example.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 게시글 UI 요청을 처리하는 컨트롤러.
 */
@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 컨트롤러 생성자.
     *
     * @param postService 게시글 서비스
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 게시글 목록 화면을 제공한다.
     *
     * @param model 모델
     * @return 목록 뷰
     */
    @GetMapping
    public String list(Model model) {
        List<Post> posts = postService.getAll();
        model.addAttribute("posts", posts);
        return "posts/list";
    }

    /**
     * 게시글 상세 화면을 제공한다.
     *
     * @param id 게시글 ID
     * @param model 모델
     * @return 상세 뷰
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.get(id);
        model.addAttribute("post", post);
        return "posts/detail";
    }

    /**
     * 게시글 생성 폼을 제공한다.
     *
     * @param model 모델
     * @return 폼 뷰
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    /**
     * 게시글을 생성한다.
     *
     * @param postForm 게시글 폼
     * @param bindingResult 검증 결과
     * @return 리다이렉트 또는 폼 뷰
     */
    @PostMapping
    public String create(@Valid PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.create(postForm.getTitle(), postForm.getContent());
        return "redirect:/posts";
    }

    /**
     * 게시글 수정 폼을 제공한다.
     *
     * @param id 게시글 ID
     * @param model 모델
     * @return 폼 뷰
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.get(id);
        PostForm postForm = new PostForm();
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());
        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id);
        return "posts/form";
    }

    /**
     * 게시글을 수정한다.
     *
     * @param id 게시글 ID
     * @param postForm 게시글 폼
     * @param bindingResult 검증 결과
     * @param model 모델
     * @return 리다이렉트 또는 폼 뷰
     */
    @PostMapping("/{id}")
    public String update(
        @PathVariable Long id,
        @Valid PostForm postForm,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            return "posts/form";
        }
        postService.update(id, postForm.getTitle(), postForm.getContent());
        return "redirect:/posts/{id}";
    }

    /**
     * 게시글을 삭제한다.
     *
     * @param id 게시글 ID
     * @return 목록 리다이렉트
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/posts";
    }
}
