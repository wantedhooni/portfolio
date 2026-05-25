package com.revy.example.admin.auth.component;


import com.revy.example.admin.auth.mapper.JwtPrincipalMapper;
import com.revy.example.admin.AdminReader;
import com.revy.example.jwt.payload.JwtPrincipal;
import com.revy.example.jwt.JwtPrincipalLoader;
import com.revy.example.jwt.enums.PrincipalType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JWT 토큰의 ADMIN 주체를 Admin 엔티티 기반 인증 주체로 복원합니다.
 */
@Component
public class AdminJwtPrincipalLoader implements JwtPrincipalLoader {

    // private final AdminRepository adminRepository;
    private final AdminReader adminReader;

    public AdminJwtPrincipalLoader(AdminReader adminReader) {
        this.adminReader = adminReader;
    }

    @Override
    public PrincipalType supports() {
        return AdminJwtPrincipal.TYPE;
    }

    @Override
    public Optional<JwtPrincipal> load(Long id) {
        return adminReader.findByIdWithRoles(id).map(JwtPrincipalMapper::toJwtPrincipal);
    }


}
