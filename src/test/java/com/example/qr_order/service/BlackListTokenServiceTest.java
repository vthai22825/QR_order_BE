package com.example.qr_order.service;

import com.example.qr_order.entity.BlackListToken;
import com.example.qr_order.repository.BlackListTokenRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho BlackListTokenService.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Token tồn tại/không tồn tại
 * - Decision Table: Logic kiểm tra trùng trước khi lưu
 * - White-box: Branch coverage cho cả 2 nhánh if
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BlackListTokenService Tests")
class BlackListTokenServiceTest {

    @Mock
    private BlackListTokenRepo blackListTokenRepo;

    @InjectMocks
    private BlackListTokenService blackListTokenService;

    // =========================================================================
    // TC_SVC_BLT_ADD: Kiểm thử blackList()
    // =========================================================================
    @Nested
    @DisplayName("blackList() - Thêm token vào blacklist")
    class BlackListTests {

        @Test
        @DisplayName("TC_SVC_BLT_ADD_01 - [EP Valid] Token mới → save() called")
        void shouldSaveNewTokenToBlackList() {
            // Arrange
            String token = "eyJhbGciOiJIUzUxMiJ9.test.token";
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            when(blackListTokenRepo.existsByToken(token)).thenReturn(false);

            // Act
            blackListTokenService.blackList(token, expiresAt);

            // Assert
            verify(blackListTokenRepo).save(any(BlackListToken.class));
        }

        @Test
        @DisplayName("TC_SVC_BLT_ADD_02 - [Decision Table] Token đã tồn tại → Không lưu lại")
        void shouldNotSaveDuplicateToken() {
            // Arrange
            String token = "eyJhbGciOiJIUzUxMiJ9.existing.token";
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            when(blackListTokenRepo.existsByToken(token)).thenReturn(true);

            // Act
            blackListTokenService.blackList(token, expiresAt);

            // Assert
            verify(blackListTokenRepo, never()).save(any(BlackListToken.class));
        }
    }

    // =========================================================================
    // TC_SVC_BLT_CHECK: Kiểm thử isBlackList()
    // =========================================================================
    @Nested
    @DisplayName("isBlackList() - Kiểm tra token trong blacklist")
    class IsBlackListTests {

        @Test
        @DisplayName("TC_SVC_BLT_CHECK_01 - [EP Valid] Token nằm trong blacklist → true")
        void shouldReturnTrueWhenTokenIsBlackListed() {
            // Arrange
            when(blackListTokenRepo.existsByToken("blocked.token")).thenReturn(true);

            // Act
            boolean result = blackListTokenService.isBlackList("blocked.token");

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("TC_SVC_BLT_CHECK_02 - [EP Valid] Token không trong blacklist → false")
        void shouldReturnFalseWhenTokenIsNotBlackListed() {
            // Arrange
            when(blackListTokenRepo.existsByToken("valid.token")).thenReturn(false);

            // Act
            boolean result = blackListTokenService.isBlackList("valid.token");

            // Assert
            assertFalse(result);
        }
    }
}
