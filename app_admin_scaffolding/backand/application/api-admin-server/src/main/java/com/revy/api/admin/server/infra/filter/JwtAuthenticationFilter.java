package com.revy.api.admin.server.infra.filter;

import com.revy.application.facade.administrator.admin.AdminReader;
import com.revy.application.facade.administrator.admin.dto.AdminReaderDto;
import com.revy.jwt.provider.JwtTokenProvider;
import com.revy.security.filter.AbstractJwtAuthenticationFilter;
import com.revy.security.pricipal.Principal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter<AdminReaderDto.AuthAdmin> {

    private final AdminReader adminReader;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, AdminReader adminReader) {
        super(jwtTokenProvider);
        this.adminReader = adminReader;
    }

    @Override
    protected Optional<AdminReaderDto.AuthAdmin> resolvePrincipal(String userId) {
        UUID adminId;
        try {
            adminId = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return adminReader.getAuthAdminById(adminId);
    }

    @Override
    protected boolean isActivePrincipal(AdminReaderDto.AuthAdmin principal) {
        return principal.enabled();
    }

    @Override
    protected Authentication toAuthentication(AdminReaderDto.AuthAdmin admin) {
        Principal principal = convert(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    protected String extractMdcUserId(AdminReaderDto.AuthAdmin principal) {
        return String.valueOf(principal.id());
    }

    private Principal convert(AdminReaderDto.AuthAdmin admin) {
        return Principal.createPrincipal(admin.id(), admin.email(), admin.encodedPassword(), admin.roleNames());
    }
}
