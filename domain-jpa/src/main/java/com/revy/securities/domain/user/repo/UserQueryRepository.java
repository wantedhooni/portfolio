package com.revy.securities.domain.user.repo;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.user.QUser;
import com.revy.securities.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * Querydsl로 이메일을 기준으로 사용자를 조회한다.
     *
     * @param email 사용자 이메일
     * @return 사용자 정보
     */
    public Optional<User> findByEmail(String email) {
        QUser user = QUser.user;
        return Optional.ofNullable(queryFactory.selectFrom(user)
                                               .where(user.email.eq(email))
                                               .fetchOne());
    }
}
