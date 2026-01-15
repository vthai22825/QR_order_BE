package com.example.qr_order.security.user;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long user_id;
    private final String user_name;
    private final String password_hash;
    private final String role;
    private final boolean is_active;

    public Long getId() {
        return user_id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security convention: ROLE_*
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password_hash;
    }

    @Override
    public String getUsername() {
        return user_name;
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
        return is_active;
    }
}
