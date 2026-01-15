package com.example.qr_order.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String user_name;
    private String password;
}
