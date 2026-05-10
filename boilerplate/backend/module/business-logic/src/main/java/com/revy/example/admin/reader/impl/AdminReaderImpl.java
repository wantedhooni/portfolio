package com.revy.example.admin.reader.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.admin.reader.AdminReader;
import com.revy.example.doamin.admin.Admin;
import com.revy.example.doamin.admin.QAdmin;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public void search(){
        BooleanBuilder where = new BooleanBuilder();
        where.and(ADMIN.email.eq("admin"));
    }
}
