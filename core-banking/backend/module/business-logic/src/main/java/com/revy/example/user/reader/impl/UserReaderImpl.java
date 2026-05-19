package com.revy.example.user.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.domain.admin.Admin;
import com.revy.example.domain.user.QUser;
import com.revy.example.domain.user.User;
import com.revy.example.user.reader.UserReader;
import com.revy.example.utils.QuerydslUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
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

    @Override
    public Page<User> search(Pageable pageable, String name) {

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.eq(USER.name, name));

        List<User> content = jpaQueryFactory.selectFrom(USER)
                                             .where(where)
                                             .orderBy(USER.id.desc())
                                             .offset(pageable.getOffset())
                                             .limit(pageable.getPageSize())
                                             .fetch();

        Long total = jpaQueryFactory.select(USER.count())
                                    .from(USER)
                                    .where(where)
                                    .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }


}
