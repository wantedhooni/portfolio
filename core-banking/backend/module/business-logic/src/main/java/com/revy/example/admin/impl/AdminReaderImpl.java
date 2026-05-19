package com.revy.example.admin.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.admin.AdminReader;
import com.revy.example.domain.admin.Admin;
import com.revy.example.domain.admin.QAdmin;
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
public class AdminReaderImpl implements AdminReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QAdmin ADMIN = QAdmin.admin;

    public AdminReaderImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<Admin> findById(Long id) {
        Admin result = jpaQueryFactory.selectFrom(ADMIN).where(ADMIN.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        Admin result = jpaQueryFactory.selectFrom(ADMIN).where(ADMIN.email.eq(email)).fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<Admin> search(Pageable pageable, String name) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.like(ADMIN.name, name));

        List<Admin> content = jpaQueryFactory.selectFrom(ADMIN)
                                             .where(where)
                                             .orderBy(ADMIN.id.desc())
                                             .offset(pageable.getOffset())
                                             .limit(pageable.getPageSize())
                                             .fetch();

        Long total = jpaQueryFactory.select(ADMIN.count())
                                    .from(ADMIN)
                                    .where(where)
                                    .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
