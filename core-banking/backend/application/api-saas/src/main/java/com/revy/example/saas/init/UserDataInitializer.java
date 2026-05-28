package com.revy.example.saas.init;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.domain.account.enums.AccountType;
import com.revy.example.user.command.UserCommand;
import com.revy.example.user.command.dto.RegisterUserCommand;
import com.revy.example.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 시작 시 데모 유저와 초기 계좌를 생성합니다.
 * 이미 존재하는 경우 건너뜁니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements ApplicationRunner {

    private static final String USER_DEMO_EMAIL    = "demo@example.com";
    private static final String USER_DEMO_PASSWORD = "Qwer1234!";

    /** 데모 계좌에 자동 생성할 통화 목록 */
    private static final List<String> DEMO_CURRENCIES = List.of("USD", "KRW", "JPY");

    private final UserReader      userReader;
    private final UserCommand     userCommand;
    private final AccountCommand  accountCommand;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userReader.existsByEmail(USER_DEMO_EMAIL)) {
            log.info("[UserDataInitializer] 데모 유저가 이미 존재합니다. 건너뜁니다.");
            return;
        }

        Long userId = userCommand.register(new RegisterUserCommand(
            USER_DEMO_EMAIL,
            passwordEncoder.encode(USER_DEMO_PASSWORD),
            "demoUser"
        ));
        log.info("[UserDataInitializer] 데모 유저 생성 완료. id={}", userId);

        // 통화별 기본 계좌 자동 생성
        for (String currency : DEMO_CURRENCIES) {
            try {
                Long accountId = accountCommand.openAccount(new OpenAccountCommand(
                    userId,
                    currency + " Wallet",
                    AccountType.REAL,
                    currency
                ));
                log.info("[UserDataInitializer] 데모 계좌 생성 완료. currency={} accountId={}", currency, accountId);
            } catch (Exception e) {
                log.warn("[UserDataInitializer] 데모 계좌 생성 실패. currency={} error={}", currency, e.getMessage());
            }
        }
    }
}
