package com.revy.api.server.infra.filter;

import com.revy.application.facade.user.UserReader;
import com.revy.application.facade.user.dto.UserReaderDto;
import com.revy.jwt.provider.JwtTokenProvider;
import com.revy.security.filter.AbstractJwtAuthenticationFilter;
import com.revy.security.pricipal.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationFilter
        extends AbstractJwtAuthenticationFilter<UserReaderDto.AuthUser> {

    private final UserReader reader;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserReader reader) {
        super(jwtTokenProvider);
        this.reader = reader;
    }

    @Override
    protected Optional<UserReaderDto.AuthUser> resolvePrincipal(String userId) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return reader.getByAuthUserId(id);
    }

    @Override
    protected boolean isActivePrincipal(UserReaderDto.AuthUser principal) {
        return principal.enabled();
    }

    @Override
    protected Authentication toAuthentication(UserReaderDto.AuthUser admin) {
        Principal principal = convert(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    protected String extractMdcUserId(UserReaderDto.AuthUser principal) {
        return String.valueOf(principal.id());
    }

    private Principal convert(UserReaderDto.AuthUser user) {
        return Principal.createPrincipal(user.id(), user.email(), user.encodedPassword(), user.roleNames());
    }
}
