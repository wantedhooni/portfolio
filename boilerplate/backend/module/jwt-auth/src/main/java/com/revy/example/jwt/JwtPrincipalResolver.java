package com.revy.example.jwt;

import com.revy.example.jwt.enums.PrincipalType;
import com.revy.example.jwt.payload.JwtPrincipal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 등록된 인증 주체 로더 중 토큰의 principalType에 맞는 로더를 찾아 위임합니다. */
@Component
public class JwtPrincipalResolver {

    private final Map<PrincipalType, JwtPrincipalLoader> loaders;

    public JwtPrincipalResolver(List<JwtPrincipalLoader> loaders) {
        this.loaders = loaders.stream()
                .collect(Collectors.toUnmodifiableMap(JwtPrincipalLoader::supports, Function.identity()));
    }


    public Optional<JwtPrincipal> resolve(String principalType, Long principalId) {
        return resolve(PrincipalType.valueOf(principalType), principalId);
    }
    public Optional<JwtPrincipal> resolve(PrincipalType principalType, Long principalId) {
        JwtPrincipalLoader loader = loaders.get(principalType);
        if (loader == null) {
            return Optional.empty();
        }
        return loader.load(principalId);
    }
}
