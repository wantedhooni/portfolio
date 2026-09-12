package com.revy.entity.domain

import com.revy.entity.common.BaseEntityComponent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = false)
class BookHandler : BaseEntityComponent() {

    fun createBook(title: String, author: String) : Book {
        val newBook = Book.createNewBook(title, author)
        em.persist(Book.createNewBook(title, author))
        return newBook;
    }

}