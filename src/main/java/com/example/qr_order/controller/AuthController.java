package com.example.qr_order.controller;

import com.example.qr_order.dtos.LoginRequest;

import com.example.qr_order.dtos.response.AuthResponse;
import com.example.qr_order.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<com.example.qr_order.dtos.response.MessageResponse> changePassword(
            @Valid @RequestBody com.example.qr_order.dtos.ChangePasswordRequest request) {

        // Get current user ID from SecurityContext
        com.example.qr_order.security.user.CustomUserDetails userDetails = (com.example.qr_order.security.user.CustomUserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        com.example.qr_order.dtos.response.MessageResponse response = authService.changePassword(userDetails.getId(),
                request);
        return ResponseEntity.ok(response);
    }

}
