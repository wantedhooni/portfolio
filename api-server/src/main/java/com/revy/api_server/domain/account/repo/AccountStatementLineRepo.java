package com.revy.api_server.domain.account.repo;

import com.revy.api_server.domain.account.AccountStatementLine;
import com.revy.api_server.domain.account.repo.query.AccountStatementLineQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountStatementLineRepo extends JpaRepository<AccountStatementLine, UUID>, AccountStatementLineQueryRepo {
}
