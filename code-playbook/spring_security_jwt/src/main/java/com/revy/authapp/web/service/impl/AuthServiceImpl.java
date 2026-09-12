package com.revy.authapp.web.service.impl;

import com.revy.authapp.domain.user.Role;
import com.revy.authapp.domain.user.User;
import com.revy.authapp.domain.user.UserDetail;
import com.revy.authapp.domain.user.UserStatus;
import com.revy.authapp.domain.user.repo.RoleRepository;
import com.revy.authapp.domain.user.repo.UserDetailRepository;
import com.revy.authapp.domain.user.repo.UserQueryRepository;
import com.revy.authapp.domain.user.repo.UserRepository;
import com.revy.authapp.security.provider.JwtTokenProvider;
import com.revy.authapp.security.token.TokenStore;
import com.revy.authapp.web.common.ApiException;
import com.revy.authapp.web.common.ErrorCode;
import com.revy.authapp.web.service.AuthService;
import com.revy.authapp.web.service.dto.LoginCommand;
import com.revy.authapp.web.service.dto.LoginResult;
import com.revy.authapp.web.service.dto.SignupCommand;
import com.revy.authapp.web.service.dto.impl.LoginResultImpl;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final String TOKEN_TYPE = "Bearer";
    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserDetailRepository userDetailRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;

    @Override
    public Long signup(SignupCommand signupCommand) {
        userQueryRepository.findByEmail(signupCommand.getEmail()).ifPresent(user -> {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        });
        User user = new User();
        user.setEmail(signupCommand.getEmail());
        user.setPassword(passwordEncoder.encode(signupCommand.getPassword()));
        Role userRole = roleRepository.findByName("USER")
                                      .orElseThrow(() -> new IllegalStateException("기본 ROLE(USER)가 없습니다."));
        user.addRole(userRole);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        UserDetail detail = UserDetail.builder().user(savedUser).name(signupCommand.getName())
                                      .phone(signupCommand.getPhone()).address(signupCommand.getAddress()).build();
        userDetailRepository.save(detail);
        return savedUser.getId();
    }

    @Transactional
    public LoginResult login(LoginCommand loginCommand) {
        User user = userQueryRepository.findByEmail(loginCommand.getEmail())
                                       .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (!passwordEncoder.matches(loginCommand.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        tokenStore.saveRefreshToken(user.getId(), refreshToken, Duration.between(java.time.Instant.now(), jwtTokenProvider.getExpiration(refreshToken)));
        return LoginResultImpl.builder().tokenType(TOKEN_TYPE).accessToken(accessToken).refreshToken(refreshToken)
                              .build();
    }

    @Override
    public LoginResult reissue(String currentRefreshToken) {
        Assert.hasText(currentRefreshToken, "currentRefreshToken is empty.");
        if (!jwtTokenProvider.validateToken(currentRefreshToken)) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Long userId = jwtTokenProvider.getUserId(currentRefreshToken);
        String storedToken = tokenStore.findRefreshToken(userId)
                                       .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        if (!storedToken.equals(currentRefreshToken)) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        String newAccessToken = jwtTokenProvider.createAccessToken(user), newRefreshToken = jwtTokenProvider.createRefreshToken(user);
        tokenStore.saveRefreshToken(userId, newRefreshToken, Duration.between(java.time.Instant.now(), jwtTokenProvider.getExpiration(newRefreshToken)));
        return LoginResultImpl.builder().tokenType(TOKEN_TYPE).accessToken(newAccessToken).refreshToken(newRefreshToken)
                              .build();
    }

    @Override
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) throw new ApiException(ErrorCode.INVALID_TOKEN);
        Duration ttl = Duration.between(java.time.Instant.now(), jwtTokenProvider.getExpiration(accessToken));
        tokenStore.blacklistAccessToken(accessToken, ttl);
    }


    @Transactional
    @Override
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.withdraw();
        tokenStore.deleteRefreshToken(userId);
    }
}
