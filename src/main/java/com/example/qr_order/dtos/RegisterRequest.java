package com.example.qr_order.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String userName;
    private String password;
    private String confirmPassword;
    private String fullName;
}
