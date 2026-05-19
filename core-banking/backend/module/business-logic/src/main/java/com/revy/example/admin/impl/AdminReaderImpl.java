package com.revy.example.admin.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.admin.AdminReader;
import com.revy.example.admin.dto.AdminCredentialResult;
import com.revy.example.admin.dto.AdminResult;
import com.revy.example.domain.admin.Admin;
import com.revy.example.domain.admin.QAdmin;
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
public class AdminReaderImpl implements AdminReader {

    private final JPAQueryFactory jpaQueryFactory;

    private final QAdmin ADMIN = QAdmin.admin;

    @Override
    public Optional<AdminResult> findById(Long id) {
        Admin admin = jpaQueryFactory.selectFrom(ADMIN).where(ADMIN.id.eq(id)).fetchOne();
        return Optional.ofNullable(admin).map(AdminResult::from);
    }

    @Override
    public Optional<AdminResult> findByEmail(String email) {
        Admin admin = jpaQueryFactory.selectFrom(ADMIN).where(ADMIN.email.eq(email)).fetchOne();
        return Optional.ofNullable(admin).map(AdminResult::from);
    }

    @Override
    public Optional<AdminCredentialResult> findCredentialByEmail(String email) {
        Admin admin = jpaQueryFactory.selectFrom(ADMIN).where(ADMIN.email.eq(email)).fetchOne();
        return Optional.ofNullable(admin).map(AdminCredentialResult::from);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaQueryFactory.selectOne()
                              .from(ADMIN)
                              .where(ADMIN.email.eq(email))
                              .fetchFirst() != null;
    }

    @Override
    public Page<AdminResult> search(Pageable pageable, String name) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.like(ADMIN.name, name));

        List<Admin> content = jpaQueryFactory.selectFrom(ADMIN)
            .where(where)
            .orderBy(ADMIN.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ADMIN.count())
            .from(ADMIN)
            .where(where);

        return PageableExecutionUtils.getPage(
            content.stream().map(AdminResult::from).toList(),
            pageable,
            () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
