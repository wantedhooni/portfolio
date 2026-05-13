package com.revy.example.saas.auth.compoenet;

import com.revy.example.jwt.JwtPrincipalLoader;
import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.saas.auth.mapper.JwtPrincipalMapper;
import com.revy.example.user.reader.UserReader;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JWT 토큰의 ADMIN 주체를 Admin 엔티티 기반 인증 주체로 복원합니다.
 */
@Component
public class UserJwtPrincipalLoader implements JwtPrincipalLoader {

    // private final AdminRepository adminRepository;
    private final UserReader userReader;

    public UserJwtPrincipalLoader(UserReader userReader) {
        this.userReader = userReader;
    }

    @Override
    public PrincipalType supports() {
        return PrincipalType.USER;
    }

    @Override
    public Optional<JwtPrincipal> load(Long id) {
        return userReader.findById(id).map(JwtPrincipalMapper::toJwtPrincipal);
    }


}
