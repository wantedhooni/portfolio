package com.revy.entity.domain

import com.querydsl.core.types.Projections
import com.revy.entity.common.BaseEntityComponent
import com.revy.entity.domain.dto.BookDto
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class BookReader(
    val repository: BookRepository
) : BaseEntityComponent() {


    fun findAllByProjectionsBean(): List<BookDto> {
        return jpaQueryFactory.select(
            Projections.bean(
                BookDto::class.java,
                book.id.`as`(BookDto::id.name),
                book.title.`as`(BookDto::title.name),
                book.author.`as`(BookDto::author.name)
            ))
        .from(book).fetch()
    }

    fun findAllByProjectionsConstructor(): List<BookDto> {
        return jpaQueryFactory.select(
            Projections.constructor(
                BookDto::class.java,
                book.id,
                book.title,
                book.author)
        ).from(book)
            .fetch()

    }

    companion object {
        val book = QBook.book
    }

}
