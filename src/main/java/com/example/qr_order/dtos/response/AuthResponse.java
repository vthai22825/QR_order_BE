package com.example.qr_order.dtos.response;

import com.example.qr_order.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String userName;
    private Role role;
    private String accessToken;
}
