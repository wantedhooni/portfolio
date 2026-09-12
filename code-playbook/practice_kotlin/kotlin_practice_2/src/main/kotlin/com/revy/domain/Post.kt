package com.revy.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Entity
@Table(name="post")
class Post {
    @OptIn(ExperimentalUuidApi::class)
    @Id
    @Column(name="id", columnDefinition = "uuid")
    val id: UUID = Uuid.generateV7().toJavaUuid()

    @Column(name="title")
    var title: String = ""

    @Column(name="content")
    var content: String = ""

    companion object {
        fun createNewPost(title: String, content: String): Post {
            var newPost = Post()
            newPost.title = title
            newPost.content = content
            return newPost

        }
    }
}