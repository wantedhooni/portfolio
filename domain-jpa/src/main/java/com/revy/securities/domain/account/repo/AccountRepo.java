package com.revy.securities.domain.account.repo;

import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.repo.query.AccountQueryRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long>, AccountQueryRepo {
    @Modifying
    @Query(
            value = """
            UPDATE Account account SET account.cashBalance = account.cashBalance + :amount,
            account.availableCash = account.availableCash + :amount
            WHERE account.accountNo = :accountNo
            """)
    void addAllBalance(String accountNo, BigDecimal amount);

    @Modifying
    @Query(
            value = """
            UPDATE Account account SET account.availableCash = account.availableCash - :amount 
            WHERE account.accountNo = :accountNo and account.ownerId = :ownerId
            """)
    void subtractAvailableCashBalance(Long ownerId, String accountNo, BigDecimal amount);

    @Modifying
    @Query(
            value = """
            UPDATE Account account SET account.availableCash = account.availableCash + :amount 
            WHERE account.accountNo = :accountNo and account.ownerId = :ownerId
            """)
    void addAvailableCashBalance(Long ownerId, String accountNo, BigDecimal amount);
}
