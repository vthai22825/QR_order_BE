package com.example.qr_order.security.user;

import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    // 1. Khai báo các biến (Thứ tự này QUYẾT ĐỊNH thứ tự trong hàm khởi tạo)
    private Long id;
    private String userName;
    @JsonIgnore
    private String password;
    private Role role;
    private boolean isActive;

    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails create(User user) {
        // Tạo Authority từ Role
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new CustomUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive(),
                authorities
        );
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
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
        return isActive;
    }
}