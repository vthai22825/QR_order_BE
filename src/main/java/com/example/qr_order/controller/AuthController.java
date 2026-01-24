package com.example.qr_order.controller;


import com.example.qr_order.dtos.LoginRequest;
import com.example.qr_order.dtos.RegisterRequest;
import com.example.qr_order.dtos.response.AuthResponse;
import com.example.qr_order.dtos.response.MessageResponse;
import com.example.qr_order.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        MessageResponse response = authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(authResponse);
    }


}
