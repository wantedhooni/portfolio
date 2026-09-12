package com.revy.security.pricipal;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Principal implements UserDetails {
    private final UUID id;

    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;

    private Principal(UUID id, String username, String password, Set<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static Principal createPrincipal(UUID id, String username, String password, Set<String> role) {
        Set<SimpleGrantedAuthority> authorities = role.stream().map(
                SimpleGrantedAuthority::new
        ).collect(Collectors.toSet());
        return new Principal(id, username, password, authorities);
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
