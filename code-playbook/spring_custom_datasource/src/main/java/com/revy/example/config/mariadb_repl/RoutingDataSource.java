package com.revy.example.config.mariadb_repl;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 읽기 전용 여부에 따라 데이터소스를 라우팅하는 클래스.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 현재 트랜잭션 상태에 맞는 데이터소스 키를 반환한다.
     *
     * @return 데이터소스 키
     */
    @Override
    protected Object determineCurrentLookupKey() {
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return readOnly ? DataSourceKey.SECONDARY : DataSourceKey.PRIMARY;
    }
}
