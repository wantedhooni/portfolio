package com.revy.entity.domain

import com.revy.entity.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*


@Entity
@Table(name = "books")
class Book : BaseEntity() {
    var title: String = ""
    var author: String = ""

    companion object {
        fun createNewBook(title: String, author: String): Book {
            var newBook = Book()
            newBook.id = UUID.randomUUID()
            newBook.title = title
            newBook.author = author
            return newBook
        }
    }

    override fun toString(): String {
        return "Book(title='$title', author='$author') ${super.toString()}"
    }

}