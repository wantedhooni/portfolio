package com.revy.securities.domain.account.repo;

import com.revy.securities.domain.account.AccountStatementLine;
import com.revy.securities.domain.account.repo.query.AccountStatementLineQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountStatementLineRepo extends JpaRepository<AccountStatementLine, UUID>, AccountStatementLineQueryRepo {
}
