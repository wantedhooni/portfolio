package com.example.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.example.auth.config.JwtProps;
import com.example.auth.entity.AuthUserEntity;
import com.example.auth.repo.AuthUserRepository;
import com.example.common.error.ApiException;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class AuthService {

    private final JwtProps props;
    private final JwtEncoder encoder;
    private final AuthUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtProps props, AuthUserRepository users, PasswordEncoder passwordEncoder) {
        this.props = props;
        this.users = users;
        this.passwordEncoder = passwordEncoder;

        SecretKey key = new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public String issueToken(String username, String password) {
        AuthUserEntity u = users.findByUsername(username)
                .orElseThrow(() -> new ApiException("AUTH_INVALID", "Invalid username or password"));

        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new ApiException("AUTH_INVALID", "Invalid username or password");
        }

        List<String> roles = Arrays.stream(u.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.expiresSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(username)
                .claim("roles", roles)
                .build();

        var headers = org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public long expiresSeconds() {
        return props.expiresSeconds();
    }
}
