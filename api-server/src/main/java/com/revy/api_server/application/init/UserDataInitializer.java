package com.revy.api_server.application.init;


import com.revy.securities.domain.user.Authority;
import com.revy.securities.domain.user.Role;
import com.revy.securities.domain.user.User;
import com.revy.securities.domain.user.UserDetail;
import com.revy.securities.domain.user.UserStatus;
import com.revy.securities.domain.user.repo.AuthorityRepository;
import com.revy.securities.domain.user.repo.RoleRepository;
import com.revy.securities.domain.user.repo.UserDetailRepository;
import com.revy.securities.domain.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (authorityRepository.count() == 0) {
            authorityRepository.save(new Authority("USER_READ", "사용자 조회 권한"));
            authorityRepository.save(new Authority("USER_WRITE", "사용자 수정 권한"));
            authorityRepository.save(new Authority("ADMIN_READ", "관리자 조회 권한"));
            authorityRepository.save(new Authority("ADMIN_WRITE", "관리자 수정 권한"));
        }

        if (roleRepository.count() == 0) {
            Authority userRead = authorityRepository.findByName("USER_READ")
                                                    .orElseThrow(() -> new IllegalStateException("USER_READ 권한이 없습니다."));
            Authority userWrite = authorityRepository.findByName("USER_WRITE")
                                                     .orElseThrow(() -> new IllegalStateException("USER_WRITE 권한이 없습니다."));
            Authority adminRead = authorityRepository.findByName("ADMIN_READ")
                                                     .orElseThrow(() -> new IllegalStateException("ADMIN_READ 권한이 없습니다."));
            Authority adminWrite = authorityRepository.findByName("ADMIN_WRITE")
                                                      .orElseThrow(() -> new IllegalStateException("ADMIN_WRITE 권한이 없습니다."));

            Role userRole = new Role("USER");
            userRole.addAuthority(userRead);
            userRole.addAuthority(userWrite);

            Role adminRole = new Role("ADMIN");
            adminRole.addAuthority(userRead);
            adminRole.addAuthority(userWrite);
            adminRole.addAuthority(adminRead);
            adminRole.addAuthority(adminWrite);

            roleRepository.save(userRole);
            roleRepository.save(adminRole);
        }

        if (userRepository.count() > 0) {
            return;
        }
        log.info("CommandLineRunner starting...");

        Role userRole = roleRepository.findByName("USER")
                                      .orElseThrow(() -> new IllegalStateException("기본 ROLE(USER)가 없습니다."));
        Role adminRole = roleRepository.findByName("ADMIN")
                                       .orElseThrow(() -> new IllegalStateException("기본 ROLE(ADMIN)가 없습니다."));
        IntStream.rangeClosed(1, 20).forEach(index -> {
            User user = new User();
            user.setEmail("user" + index + "@example.com");
            user.setPassword(passwordEncoder.encode("Password!"));
            user.setStatus(UserStatus.ACTIVE);
            user.addRole(index % 10 == 0 ? adminRole : userRole);
            userRepository.save(user);

            UserDetail detail = UserDetail
                    .builder()
                    .user(user)
                    .name("샘플 사용자 " + index)
                    .phone("010-0000-" + String.format("%04d", index))
                    .address("서울시 샘플구 " + index + "번지")
                    .build();
            userDetailRepository.save(detail);
        });

        log.info("CommandLineRunner end");
    }
}
