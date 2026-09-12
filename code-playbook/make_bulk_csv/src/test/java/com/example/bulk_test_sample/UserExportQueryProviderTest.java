package com.example.bulk_test_sample;

import com.example.bulk_test_sample.querydsl.UserExportQueryProvider;
import com.querydsl.sql.SQLBindings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Querydsl-SQL 생성 로직을 검증한다.
 */
class UserExportQueryProviderTest {

    /**
     * given/when/then 스타일로 SQL 생성 결과를 확인한다.
     */
    @Test
    @DisplayName("Querydsl-SQL이 users 조회 SQL을 생성한다")
    void shouldBuildSqlBindings() {
        // given
        UserExportQueryProvider provider = new UserExportQueryProvider();

        // when
        SQLBindings bindings = provider.buildExportQuery();

        // then
        assertThat(bindings.getSQL()).contains("from users");
        assertThat(bindings.getNullFriendlyBindings()).isEmpty();
    }
}
