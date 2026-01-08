package com.example.bulk_test_sample.querydsl;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Timestamp;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * users 테이블에 대한 Querydsl-SQL 메타정보.
 */
public class QUsers extends RelationalPathBase<QUsers> {

    /**
     * users 테이블의 기본 별칭.
     */
    public static final QUsers users = new QUsers("users");

    /**
     * users.id 컬럼.
     */
    public final NumberPath<Long> id = createNumber("id", Long.class);

    /**
     * users.email 컬럼.
     */
    public final StringPath email = createString("email");

    /**
     * users.name 컬럼.
     */
    public final StringPath name = createString("name");

    /**
     * users.age 컬럼.
     */
    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    /**
     * users.created_at 컬럼.
     */
    public final DateTimePath<Timestamp> createdAt = createDateTime("created_at", Timestamp.class);

    /**
     * 테이블 메타데이터를 생성한다.
     *
     * @param variable 테이블 별칭
     */
    public QUsers(String variable) {
        super(QUsers.class, forVariable(variable), null, variable);
    }

    /**
     * 테이블 메타데이터를 생성한다.
     *
     * @param metadata 메타데이터
     */
    public QUsers(PathMetadata metadata) {
        super(QUsers.class, metadata, null, metadata.getName());
    }
}
