package com.example.qr_order.dtos;

import com.example.qr_order.enums.Role;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class RegisterRequest {
    private String userName;
    private String password;
    private String confirmPassword;
    private String fullName;
}
