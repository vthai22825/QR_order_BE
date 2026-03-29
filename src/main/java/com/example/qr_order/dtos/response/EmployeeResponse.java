package com.example.qr_order.dtos.response;

import com.example.qr_order.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
    private Long userId;
    private String fullName;
    private String userName;
    private Role role;
    private boolean isActive;
}
