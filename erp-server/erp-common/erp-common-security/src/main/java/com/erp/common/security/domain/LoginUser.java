package com.erp.common.security.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LoginUser implements UserDetails, Serializable {
    private final UUID userId;
    private final String username;
    private final String password;
    private final String realName;
    private final boolean enabled;
    private final List<String> roles;
    private final List<String> permissions;

    public LoginUser(UUID userId,
                     String username,
                     String password,
                     String realName,
                     boolean enabled,
                     List<String> roles,
                     List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.enabled = enabled;
        this.roles = roles == null ? List.of() : roles;
        this.permissions = permissions == null ? List.of() : permissions;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRealName() {
        return realName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
