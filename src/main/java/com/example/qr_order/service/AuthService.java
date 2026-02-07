package com.example.qr_order.service;

import com.example.qr_order.dtos.LoginRequest;

import com.example.qr_order.dtos.response.AuthResponse;
import com.example.qr_order.dtos.response.MessageResponse;
import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import com.example.qr_order.security.JwtTokenProvider;
import java.util.regex.Pattern;

import com.example.qr_order.security.user.CustomUserDetails;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    // Phần tạo tài khoản cho nhân viên

    @Transactional(rollbackFor = Exception.class)

    public MessageResponse createEmployee(com.example.qr_order.dtos.CreateEmployeeRequest request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be null");
        }

        String userName = request.getUserName();
        String password = request.getPassword();
        String fullName = request.getFullName();
        Role role = request.getRole();

        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (userRepo.existsByUserName(userName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (password == null || !Pattern.matches(PASSWORD_PATTERN, password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must have at least 8 characters, 1 Upper, 1 Lower and 1 special character ");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        if (role == null || role == Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role for employee");
        }

        User newUser = new User();

        newUser.setUserName(userName);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setFullName(fullName);
        newUser.setActive(true); // Active immediately
        newUser.setRole(role);

        userRepo.save(newUser);

        return new MessageResponse("Employee account created successfully.");
    }

    
    // Phần đăng nhập
    public AuthResponse login(LoginRequest loginRequest) {

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtTokenProvider.generateToken(userDetails);

            return new AuthResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getRole(),
                    accessToken);

        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled. Please contact support.");

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed: " + e.getMessage());
        }
    }

}
