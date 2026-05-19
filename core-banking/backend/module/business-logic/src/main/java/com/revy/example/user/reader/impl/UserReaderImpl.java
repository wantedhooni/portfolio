package com.revy.example.user.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.user.QUser;
import com.revy.example.domain.user.User;
import com.revy.example.user.reader.UserReader;
import com.revy.example.user.reader.dto.UserCredentialResult;
import com.revy.example.user.reader.dto.UserResult;
import com.revy.example.utils.QuerydslUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReaderImpl implements UserReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUser USER = QUser.user;

    @Override
    public Optional<UserResult> findById(Long id) {
        User user = jpaQueryFactory.selectFrom(USER).where(USER.id.eq(id)).fetchOne();
        return Optional.ofNullable(user).map(UserResult::from);
    }

    @Override
    public Optional<UserResult> findByEmail(String email) {
        User user = jpaQueryFactory.selectFrom(USER).where(USER.email.eq(email)).fetchOne();
        return Optional.ofNullable(user).map(UserResult::from);
    }

    @Override
    public Optional<UserCredentialResult> findCredentialByEmail(String email) {
        User user = jpaQueryFactory.selectFrom(USER).where(USER.email.eq(email)).fetchOne();
        return Optional.ofNullable(user).map(UserCredentialResult::from);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaQueryFactory.selectOne()
                              .from(USER)
                              .where(USER.email.eq(email))
                              .fetchFirst() != null;
    }

    @Override
    public Page<UserResult> search(Pageable pageable, String name) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.like(USER.name, name));

        List<User> content = jpaQueryFactory.selectFrom(USER)
            .where(where)
            .orderBy(USER.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(USER.count())
            .from(USER)
            .where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(UserResult::from).toList(),
            pageable,
            () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
