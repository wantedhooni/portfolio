package com.revy.service

import com.revy.domain.Post
import com.revy.domain.PostRepository
import com.revy.service.payload.PostCreateCommand
import com.revy.service.payload.PostResponse
import org.springframework.http.RequestEntity.post
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class PostService(val repository: PostRepository) {

    @Transactional(readOnly = true)
    fun findAll(): List<PostResponse> {
        return repository.findAll().map { PostResponse(it.id, it.title, it.content) }
    }

    fun save(postCreateCommand: PostCreateCommand): PostResponse {
        val newPost = repository.save(map(postCreateCommand));
        return to(newPost)
    }

    private fun map(postCreateCommand: PostCreateCommand): Post {
        return Post.createNewPost(postCreateCommand.title, postCreateCommand.content)
    }

    private fun to(newPost: Post): PostResponse {
        return PostResponse(
            newPost.id, newPost.title, newPost.content
        )
    }

    fun findById(postId: UUID): PostResponse {
        return repository.findById(postId).map { PostResponse(it.id, it.title, it.content) }.orElseThrow();
    }


}
