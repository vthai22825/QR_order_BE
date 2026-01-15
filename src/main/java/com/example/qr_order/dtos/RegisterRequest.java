package com.example.qr_order.dtos;

import com.example.qr_order.enums.Role;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequest {
    private String user_name;
    private String password;
    private String full_name;
    private Role role;
}
