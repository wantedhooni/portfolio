package com.revy.example.admin.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.example.admin.RbacReader;
import com.revy.example.admin.dto.AdminRoleResult;
import com.revy.example.domain.admin.AdminRole;
import com.revy.example.domain.admin.QAdminRole;
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
public class RbacReaderImpl implements RbacReader {

    private final JPAQueryFactory jpaQueryFactory;
    private final QAdminRole ROLE = QAdminRole.adminRole;

    @Override
    public Optional<AdminRoleResult> findRoleById(Long id) {
        AdminRole role = jpaQueryFactory.selectFrom(ROLE).where(ROLE.id.eq(id)).fetchOne();
        return Optional.ofNullable(role).map(AdminRoleResult::from);
    }

    @Override
    public Optional<AdminRoleResult> findRoleByName(String name) {
        AdminRole role = jpaQueryFactory.selectFrom(ROLE).where(ROLE.name.eq(name)).fetchOne();
        return Optional.ofNullable(role).map(AdminRoleResult::from);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaQueryFactory.selectOne()
                              .from(ROLE)
                              .where(ROLE.name.eq(name))
                              .fetchFirst() != null;
    }

    @Override
    public Page<AdminRoleResult> searchRoles(Pageable pageable, String name) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslUtils.like(ROLE.name, name));

        List<AdminRole> content = jpaQueryFactory.selectFrom(ROLE)
                .where(where)
                .orderBy(ROLE.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(ROLE.count())
                .from(ROLE)
                .where(where);

        return PageableExecutionUtils.getPage(
                content.stream().map(AdminRoleResult::from).toList(),
                pageable,
                () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L)
        );
    }
}
