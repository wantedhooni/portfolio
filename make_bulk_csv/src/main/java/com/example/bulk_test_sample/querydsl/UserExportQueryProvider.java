package com.example.bulk_test_sample.querydsl;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Querydsl-SQL을 사용해 users 익스포트 SQL을 생성한다.
 */
@Slf4j
@Component
public class UserExportQueryProvider {

    private final SQLTemplates templates = MySQLTemplates.builder().build();

    /**
     * users 테이블을 조회하는 SQL 바인딩을 생성한다.
     *
     * @return SQL 바인딩
     */
    public SQLBindings buildExportQuery() {
        QUsers users = QUsers.users;
        SQLQuery<?> query = new SQLQuery<Void>(templates)
                .select(
                        users.id,
                        users.email,
                        users.name,
                        users.age,
                        users.createdAt.as("createdAt")
                )
                .from(users)
                .orderBy(users.id.asc());
        log.info("query.getSQL().getSQL(): {}", query.getSQL().getSQL());
        return new SQLBindings(query.getSQL().getSQL(), query.getSQL().getNullFriendlyBindings());
    }
}
