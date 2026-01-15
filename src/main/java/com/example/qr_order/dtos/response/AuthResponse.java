package com.example.qr_order.dtos.response;

import com.example.qr_order.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthResponse {
    private Long user_id;
    private String user_name;
    private Role role;
    private String access_token;
}
