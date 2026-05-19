package com.revy.example.saas.init;

import com.revy.example.user.command.UserCommand;
import com.revy.example.user.command.dto.RegisterUserCommand;
import com.revy.example.user.reader.UserReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserDataInitializer implements ApplicationRunner {

    private static final String USER_DEMO_EMAIL    = "demo@example.com";
    private static final String USER_DEMO_PASSWORD = "Qwer1234!";

    private final UserReader      userReader;
    private final UserCommand     userCommand;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(UserReader userReader,
                               UserCommand userCommand,
                               PasswordEncoder passwordEncoder) {
        this.userReader      = userReader;
        this.userCommand     = userCommand;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userReader.existsByEmail(USER_DEMO_EMAIL)) {
            return;
        }
        Long id = userCommand.register(new RegisterUserCommand(
            USER_DEMO_EMAIL,
            passwordEncoder.encode(USER_DEMO_PASSWORD),
            "demoUser"
        ));
        log.info("User Created. id={}", id);
    }
}
