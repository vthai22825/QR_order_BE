package com.example.qr_order.dtos;

import com.example.qr_order.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEmployeeRequest {
    private String userName;
    private String fullName;
    private Role role;
}
