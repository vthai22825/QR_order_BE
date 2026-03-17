package com.example.qr_order.service;

import com.example.qr_order.dtos.ChangePasswordRequest;
import com.example.qr_order.dtos.CreateEmployeeRequest;
import com.example.qr_order.dtos.response.EmployeeResponse;
import com.example.qr_order.dtos.response.MessageResponse;
import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import com.example.qr_order.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho AuthService.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Valid/Invalid cho createEmployee, changePassword, resetPassword
 * - Boundary Value Analysis (BVA): Kiểm tra blank, null inputs
 * - Decision Table: Logic đặc biệt cho role OWNER vs employee roles
 * - White-box: Branch coverage cho tất cả nhánh validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private CreateEmployeeRequest validCreateRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateEmployeeRequest();
        validCreateRequest.setUserName("cashier01");
        validCreateRequest.setFullName("Nguyễn Văn A");
        validCreateRequest.setRole(Role.CASHIER);

        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUserName("cashier01");
        existingUser.setFullName("Nguyễn Văn A");
        existingUser.setRole(Role.CASHIER);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setActive(true);
        existingUser.setFirstLogin(true);
    }

    // =========================================================================
    // TC_SVC_AUTH_CREATE: Kiểm thử createEmployee()
    // =========================================================================
    @Nested
    @DisplayName("createEmployee() - Tạo tài khoản nhân viên")
    class CreateEmployeeTests {

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_01 - [EP Valid] Tạo employee CASHIER thành công")
        void shouldCreateEmployeeSuccessfully() {
            // Arrange
            when(userRepo.existsByUserName("cashier01")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            // Act
            MessageResponse result = authService.createEmployee(validCreateRequest);

            // Assert
            assertNotNull(result);
            assertTrue(result.getMessage().contains("successfully"));
            verify(userRepo).save(any(User.class));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_02 - [EP Invalid] Request null → BAD_REQUEST")
        void shouldThrowExceptionWhenRequestIsNull() {
            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(null));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_03 - [BVA Invalid] Username blank → BAD_REQUEST")
        void shouldThrowExceptionWhenUsernameIsBlank() {
            // Arrange
            validCreateRequest.setUserName("  ");

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(validCreateRequest));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_04 - [EP Invalid] Username đã tồn tại → CONFLICT")
        void shouldThrowConflictWhenUsernameAlreadyExists() {
            // Arrange
            when(userRepo.existsByUserName("cashier01")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(validCreateRequest));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_05 - [BVA Invalid] FullName blank → BAD_REQUEST")
        void shouldThrowExceptionWhenFullNameIsBlank() {
            // Arrange
            validCreateRequest.setFullName("  ");

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(validCreateRequest));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_06 - [Decision Table] Role = OWNER → BAD_REQUEST")
        void shouldThrowExceptionWhenRoleIsOwner() {
            // Arrange
            validCreateRequest.setRole(Role.OWNER);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(validCreateRequest));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_07 - [EP Invalid] Role = null → BAD_REQUEST")
        void shouldThrowExceptionWhenRoleIsNull() {
            // Arrange
            validCreateRequest.setRole(null);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.createEmployee(validCreateRequest));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_08 - [EP Valid] Tạo employee SERVER → Thành công")
        void shouldCreateServerRoleSuccessfully() {
            // Arrange
            validCreateRequest.setRole(Role.SERVER);
            when(userRepo.existsByUserName("cashier01")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            // Act
            MessageResponse result = authService.createEmployee(validCreateRequest);

            // Assert
            assertNotNull(result);
            verify(userRepo).save(any(User.class));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CREATE_09 - [EP Valid] Tạo employee CHEF → Thành công")
        void shouldCreateChefRoleSuccessfully() {
            // Arrange
            validCreateRequest.setRole(Role.CHEF);
            when(userRepo.existsByUserName("cashier01")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            // Act
            MessageResponse result = authService.createEmployee(validCreateRequest);

            // Assert
            assertNotNull(result);
            verify(userRepo).save(any(User.class));
        }
    }

    // =========================================================================
    // TC_SVC_AUTH_GETALL: Kiểm thử getAllEmployees()
    // =========================================================================
    @Nested
    @DisplayName("getAllEmployees() - Lấy danh sách nhân viên")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("TC_SVC_AUTH_GETALL_01 - [EP Valid] Có data → Return list")
        void shouldReturnEmployeeList() {
            // Arrange
            when(userRepo.findByRoleNotOrderByCreatedAtDesc(Role.OWNER))
                    .thenReturn(List.of(existingUser));

            // Act
            List<EmployeeResponse> result = authService.getAllEmployees();

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }
    }

    // =========================================================================
    // TC_SVC_AUTH_CHGPWD: Kiểm thử changePassword()
    // =========================================================================
    @Nested
    @DisplayName("changePassword() - Đổi mật khẩu")
    class ChangePasswordTests {

        @Test
        @DisplayName("TC_SVC_AUTH_CHGPWD_01 - [EP Valid] Đổi mật khẩu đúng quy tắc → Thành công")
        void shouldChangePasswordSuccessfully() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("VibeFoodie@123");
            request.setNewPassword("NewPass@456");
            request.setConfirmPassword("NewPass@456");

            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("VibeFoodie@123", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("NewPass@456")).thenReturn("newEncodedPassword");

            // Act
            MessageResponse result = authService.changePassword(1L, request);

            // Assert
            assertNotNull(result);
            assertTrue(result.getMessage().contains("successfully"));
            verify(userRepo).save(existingUser);
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CHGPWD_02 - [EP Invalid] Mật khẩu cũ sai → BAD_REQUEST")
        void shouldThrowExceptionWhenOldPasswordIsWrong() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("wrongPassword");
            request.setNewPassword("NewPass@456");
            request.setConfirmPassword("NewPass@456");

            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.changePassword(1L, request));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CHGPWD_03 - [EP Invalid] Confirm không khớp → BAD_REQUEST")
        void shouldThrowExceptionWhenConfirmPasswordDoesNotMatch() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("VibeFoodie@123");
            request.setNewPassword("NewPass@456");
            request.setConfirmPassword("DifferentPass@789");

            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("VibeFoodie@123", "encodedPassword")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.changePassword(1L, request));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CHGPWD_04 - [EP Invalid] Password format yếu → BAD_REQUEST")
        void shouldThrowExceptionWhenPasswordFormatIsWeak() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("VibeFoodie@123");
            request.setNewPassword("weak");  // Không đủ mạnh
            request.setConfirmPassword("weak");

            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("VibeFoodie@123", "encodedPassword")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.changePassword(1L, request));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_CHGPWD_05 - [EP Invalid] User không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest();
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.changePassword(999L, request));
        }
    }

    // =========================================================================
    // TC_SVC_AUTH_RESET: Kiểm thử resetPassword()
    // =========================================================================
    @Nested
    @DisplayName("resetPassword() - Reset mật khẩu về mặc định")
    class ResetPasswordTests {

        @Test
        @DisplayName("TC_SVC_AUTH_RESET_01 - [EP Valid] Reset password employee → Thành công")
        void shouldResetPasswordSuccessfully() {
            // Arrange
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode("VibeFoodie@123")).thenReturn("defaultEncodedPassword");

            // Act
            MessageResponse result = authService.resetPassword(1L);

            // Assert
            assertTrue(result.getMessage().contains("VibeFoodie@123"));
            verify(userRepo).save(existingUser);
        }

        @Test
        @DisplayName("TC_SVC_AUTH_RESET_02 - [Decision Table] Reset password OWNER → FORBIDDEN")
        void shouldThrowForbiddenWhenResettingOwnerPassword() {
            // Arrange
            existingUser.setRole(Role.OWNER);
            when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.resetPassword(1L));
        }

        @Test
        @DisplayName("TC_SVC_AUTH_RESET_03 - [EP Invalid] User không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenUserDoesNotExistForReset() {
            // Arrange
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.resetPassword(999L));
        }
    }
}
