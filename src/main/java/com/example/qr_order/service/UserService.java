package com.example.qr_order.service;

import com.example.qr_order.dtos.RegisterRequest;
import com.example.qr_order.dtos.response.AuthResponse;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import com.example.qr_order.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo user_repo;
    private final PasswordEncoder password_encoder;
    private final AuthenticationManager authentication_manager;
    private final JwtTokenProvider jwt_token_provider;


    public AuthResponse register(RegisterRequest register_request){
        if (register_request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "register_request is null");
        }

        String user_name = register_request.getUser_name();
        String password = register_request.getPassword();
        String full_name = register_request.getFull_name();
        Role role = register_request.getRole();

        if (user_name == null || user_name.isBlank() || password == null || password.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user_name/password is required");
        }

        if (role)
    }



}
