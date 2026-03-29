package com.example.qr_order.service;

import com.example.qr_order.dtos.LoginRequest;

import com.example.qr_order.dtos.response.AuthResponse;
import com.example.qr_order.dtos.response.MessageResponse;
import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import com.example.qr_order.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
import com.example.qr_order.dtos.CreateEmployeeRequest;
import com.example.qr_order.dtos.response.EmployeeResponse;
import com.example.qr_order.dtos.ChangePasswordRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final BlackListTokenService blackListTokenService;

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    // Phần tạo tài khoản cho nhân viên

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse createEmployee(CreateEmployeeRequest request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be null");
        }

        String userName = request.getUserName();
        // Default password
        String password = "VibeFoodie@123";
        String fullName = request.getFullName();
        Role role = request.getRole();

        if (userName == null || userName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (userRepo.existsByUserName(userName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
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
        newUser.setFirstLogin(true); // Must change password
        newUser.setRole(role);

        userRepo.save(newUser);

        return new MessageResponse("Employee account created successfully. Default password is: VibeFoodie@123");
    }

    public List<EmployeeResponse> getAllEmployees() {
        List<User> employees = userRepo.findByRoleNotOrderByCreatedAtDesc(Role.OWNER);
        return employees.stream()
                .map(user -> new EmployeeResponse(
                        user.getUserId(),
                        user.getFullName(),
                        user.getUserName(),
                        user.getRole(),
                        user.isActive()))
                .toList();
    }

    public MessageResponse toggleActive(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change Owner account status");
        }

        user.setActive(!user.isActive());
        userRepo.save(user);

        String status = user.isActive() ? "activated" : "deactivated";
        return new MessageResponse("Account " + status + " successfully");
    }

    public MessageResponse logout(String token) {
        Date expiresAt = jwtTokenProvider.extractExpiration(token);
        LocalDateTime expiresAtLocal = expiresAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        blackListTokenService.blackList(token, expiresAtLocal);
        return new MessageResponse("Logged out successfully");
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

            User user = userRepo.findById(userDetails.getId()).orElseThrow();

            // Owner is never considered first login (or handled separately)
            boolean isFirstLogin = user.isFirstLogin();
            if (userDetails.getRole() == Role.OWNER) {
                isFirstLogin = false;
            }

            return new AuthResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    user.getFullName(),
                    userDetails.getRole(),
                    accessToken,
                    isFirstLogin);

        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled. Please contact support.");

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed: " + e.getMessage());
        }
    }

    public MessageResponse changePassword(Long userId,ChangePasswordRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm password does not match");
        }

        if (!Pattern.matches(PASSWORD_PATTERN, request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must have at least 8 characters, 1 Upper, 1 Lower and 1 special character ");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepo.save(user);

        return new MessageResponse("Password changed successfully.");
    }

    public MessageResponse resetPassword(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Prevent resetting Owner password via this method
        if (user.getRole() == Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot reset Owner password");
        }

        user.setPasswordHash(passwordEncoder.encode("VibeFoodie@123"));
        user.setFirstLogin(true);
        userRepo.save(user);

        return new MessageResponse("Password reset to default: VibeFoodie@123");
    }

}
