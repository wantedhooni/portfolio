package com.revy.controller

import com.revy.service.PostService
import com.revy.service.payload.PostCreateCommand
import com.revy.service.payload.PostResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/")
class PostController(val service: PostService) {

    @GetMapping("/posts")
    fun getAllPosts(): List<PostResponse>{
        return service.findAll();
    }

    @PostMapping("/posts")
    fun createNewPost(@Valid @RequestBody post: PostCreateCommand): PostResponse {
        return service.save(post)
    }


    @GetMapping("/posts/{id}")
    fun getPostById(@PathVariable(value = "id") postId: UUID): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(service.findById(postId))
    }

    /*





    @GetMapping("/posts/{id}")
    fun getPostById(@PathVariable(value = "id") postId: Long): ResponseEntity<Post> {
        return postRepository.findById(postId).map { post ->
            ResponseEntity.ok(post)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PutMapping("/posts/{id}")
    fun updatePostById(@PathVariable(value = "id") postId: Long,
                          @Valid @RequestBody newPost: Post): ResponseEntity<Post> {

        return postRepository.findById(postId).map { existingPost ->
            val updatedPost: Post = existingPost
                    .copy(title = newPost.title, content = newPost.content)

            ResponseEntity.ok().body(postRepository.save(updatedPost))
        }.orElse(ResponseEntity.notFound().build())

    }

    @DeleteMapping("/posts/{id}")
    fun deletePostById(@PathVariable(value = "id") postId: Long): ResponseEntity<Void> {

        return postRepository.findById(postId).map { post  ->
            postRepository.delete(post)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())

    }
     */


}
