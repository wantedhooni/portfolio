package com.revy.example.user.reader.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.user.QUser;
import com.revy.example.domain.user.User;
import com.revy.example.user.reader.UserReader;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserReaderImpl implements UserReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QUser USER = QUser.user;

    public UserReaderImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<User> findById(Long id) {
        User result = jpaQueryFactory.selectFrom(USER).where(USER.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        User result = jpaQueryFactory.selectFrom(USER).where(USER.email.eq(email)).fetchOne();
        return Optional.ofNullable(result);
    }
}
