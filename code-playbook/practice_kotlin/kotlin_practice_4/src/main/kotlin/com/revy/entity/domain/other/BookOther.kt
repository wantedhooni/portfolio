package com.revy.entity.domain.other

import com.revy.entity.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*


@Entity
@Table(name = "books_other")
class BookOther : BaseEntity() {
    var title: String = ""
    var author: String = ""

    companion object {
        fun createNewBook(title: String, author: String): BookOther {
            var newBook = BookOther()
            newBook.id = UUID.randomUUID()
            newBook.title = title
            newBook.author = author
            return newBook
        }
    }

    override fun toString(): String {
        return "BookOrder(title='$title', author='$author') ${super.toString()}"
    }


}