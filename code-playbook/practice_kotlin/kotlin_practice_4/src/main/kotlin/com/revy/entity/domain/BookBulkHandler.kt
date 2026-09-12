package com.revy.entity.domain

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAInsertClause
import com.revy.entity.common.BaseEntityComponent
import com.revy.entity.domain.other.QBookOther
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Component
@Transactional(readOnly = false)
class BookBulkHandler : BaseEntityComponent() {

    companion object {
        val book = QBook.book
        val bookOther = QBookOther.bookOther
        private val log = LoggerFactory.getLogger(javaClass)

    }


    fun createBook(title: String, author: String): Book {
        val newBook = Book.createNewBook(title, author)
        em.persist(Book.createNewBook(title, author))
        return newBook;
    }

    fun insertBulkBookOther() {

        for (i in 1..100) {
            jpaQueryFactory.insert(bookOther).columns(
                    bookOther.id, bookOther.author, bookOther.title, bookOther.createdAt, bookOther.updatedAt,
                ).values(UUID.randomUUID(), "auther${i}", "title${i}", Instant.now(), Instant.now()).execute();
        }
    }

    /**
     * insert into select 쿼리로 삽입
     */
    fun insertBulkBookOtherV2() {
        val subQuery = JPAExpressions.select(
                book.id, book.author, book.title, book.createdAt, book.updatedAt
            ).from(book)

        val query: JPAInsertClause = jpaQueryFactory.insert(bookOther).columns(
            bookOther.id, bookOther.author, bookOther.title, bookOther.createdAt, bookOther.updatedAt,
        ).select(subQuery)
        log.info("query: ${query.toString()}")

        query.execute()
    }
}