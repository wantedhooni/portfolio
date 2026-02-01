package com.revy.api_server.application.infra.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revy.securities.domain.user.Role;
import com.revy.securities.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 인증 정보를 담는 사용자 프린시팔.
 */
@Getter
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;

    @JsonIgnore
    private final String password;
    private final Set<String> role;
    private final Set<SimpleGrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String password, Set<String> role, Set<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
    }

    /**
     * 사용자 엔티티로부터 프린시팔을 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 사용자 프린시팔
     */
    public static UserPrincipal from(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
        Set<SimpleGrantedAuthority> authorities = user.getRoles()
                                                      .stream()
                                                      .flatMap(role -> role.getAuthorities().stream())
                                                      .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                                                      .collect(Collectors.toUnmodifiableSet());

        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), roles, authorities);
    }

    @Override
    /**
     * {@inheritDoc}
     */ public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
