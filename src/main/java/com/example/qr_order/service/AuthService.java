package com.example.qr_order.service;

import com.example.qr_order.dtos.LoginRequest;
import com.example.qr_order.dtos.RegisterRequest;
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

    // Phần Đăng ký

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse register(RegisterRequest registerRequest) {

        if (registerRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register request cannot be null");
        }

        String userName = registerRequest.getUserName();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();
        String fullName = registerRequest.getFullName();

        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "registerRequest is required");
        }
        if (userRepo.existsByUserName(userName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (password == null || !Pattern.matches(PASSWORD_PATTERN, password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must have at least 8 characters, 1 Upper, 1 Lower and 1 special character ");
        }

        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        User newUser = new User();

        newUser.setUserName(userName);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setFullName(fullName);
        newUser.setActive(false);
        newUser.setRole(Role.SERVER);

        userRepo.save(newUser);

        return new MessageResponse("Registration successful. Please wait for the Owner to approve your account.");
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
