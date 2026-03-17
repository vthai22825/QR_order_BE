package com.example.qr_order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho JwtTokenProvider.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Valid/Invalid tokens
 * - Boundary Value Analysis (BVA): Token expiration timing
 * - White-box: Coverage cho generate, extract, validate methods
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails testUserDetails;

    // Generate a secure base64-encoded key for HS512
    private static final Key TEST_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static final String TEST_SECRET_BASE64 = Base64.getEncoder().encodeToString(TEST_KEY.getEncoded());
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_BASE64, TEST_EXPIRATION);

        testUserDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_CASHIER"));
            }
            @Override
            public String getPassword() { return "password"; }
            @Override
            public String getUsername() { return "cashier01"; }
            @Override
            public boolean isAccountNonExpired() { return true; }
            @Override
            public boolean isAccountNonLocked() { return true; }
            @Override
            public boolean isCredentialsNonExpired() { return true; }
            @Override
            public boolean isEnabled() { return true; }
        };
    }

    // =========================================================================
    // TC_SEC_JWT_GEN: Kiểm thử generateToken()
    // =========================================================================
    @Nested
    @DisplayName("generateToken() - Tạo JWT token")
    class GenerateTokenTests {

        @Test
        @DisplayName("TC_SEC_JWT_GEN_01 - [EP Valid] Generate token → Non-null, non-empty")
        void shouldGenerateNonEmptyToken() {
            // Act
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        @DisplayName("TC_SEC_JWT_GEN_02 - [EP Valid] Generate token → Chứa 3 phần (header.payload.signature)")
        void shouldGenerateTokenWithThreeParts() {
            // Act
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Assert
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT token phải có 3 phần");
        }
    }

    // =========================================================================
    // TC_SEC_JWT_EXTRACT: Kiểm thử extractUsername()
    // =========================================================================
    @Nested
    @DisplayName("extractUsername() - Trích xuất username từ token")
    class ExtractUsernameTests {

        @Test
        @DisplayName("TC_SEC_JWT_EXTRACT_01 - [EP Valid] Extract username → Đúng 'cashier01'")
        void shouldExtractCorrectUsername() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Act
            String username = jwtTokenProvider.extractUsername(token);

            // Assert
            assertEquals("cashier01", username);
        }
    }

    // =========================================================================
    // TC_SEC_JWT_VALID: Kiểm thử isTokenValid()
    // =========================================================================
    @Nested
    @DisplayName("isTokenValid() - Xác thực token")
    class IsTokenValidTests {

        @Test
        @DisplayName("TC_SEC_JWT_VALID_01 - [EP Valid] Token hợp lệ, username khớp → true")
        void shouldReturnTrueForValidToken() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Act
            boolean result = jwtTokenProvider.isTokenValid(token, testUserDetails);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("TC_SEC_JWT_VALID_02 - [EP Invalid] Token hợp lệ nhưng username khác → false")
        void shouldReturnFalseForDifferentUsername() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            UserDetails differentUser = new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of(new SimpleGrantedAuthority("ROLE_CHEF"));
                }
                @Override
                public String getPassword() { return "password"; }
                @Override
                public String getUsername() { return "differentUser"; }
                @Override
                public boolean isAccountNonExpired() { return true; }
                @Override
                public boolean isAccountNonLocked() { return true; }
                @Override
                public boolean isCredentialsNonExpired() { return true; }
                @Override
                public boolean isEnabled() { return true; }
            };

            // Act
            boolean result = jwtTokenProvider.isTokenValid(token, differentUser);

            // Assert
            assertFalse(result);
        }
    }

    // =========================================================================
    // TC_SEC_JWT_EXPIRE: Kiểm thử token expiration
    // =========================================================================
    @Nested
    @DisplayName("isTokenExpired() - Kiểm tra hết hạn")
    class IsTokenExpiredTests {

        @Test
        @DisplayName("TC_SEC_JWT_EXPIRE_01 - [EP Valid] Token mới tạo → chưa hết hạn")
        void shouldNotBeExpiredForFreshToken() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Act
            boolean result = jwtTokenProvider.isTokenExpired(token);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("TC_SEC_JWT_EXPIRE_02 - [BVA Time] Token hết hạn → isTokenExpired = true")
        void shouldBeExpiredForExpiredToken() {
            // Arrange - Tạo provider với expiration = 0 (hết hạn ngay lập tức)
            JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET_BASE64, 0L);
            String token = expiredProvider.generateToken(testUserDetails);

            // Wait a tiny bit to ensure token is expired
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            // Act
            boolean result = expiredProvider.isTokenExpired(token);

            // Assert
            assertTrue(result, "Token đã hết hạn phải return true");
        }
    }

    // =========================================================================
    // TC_SEC_JWT_CLAIMS: Kiểm thử extractAllClaims()
    // =========================================================================
    @Nested
    @DisplayName("extractAllClaims() - Trích xuất claims")
    class ExtractClaimsTests {

        @Test
        @DisplayName("TC_SEC_JWT_CLAIMS_01 - [EP Valid] Extract claims → Chứa roles")
        void shouldExtractClaimsWithRoles() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Act
            Claims claims = jwtTokenProvider.extractAllClaims(token);

            // Assert
            assertNotNull(claims);
            assertEquals("cashier01", claims.getSubject());
            assertNotNull(claims.get("roles"));
        }

        @Test
        @DisplayName("TC_SEC_JWT_CLAIMS_02 - [EP Valid] Extract expiration → Non-null")
        void shouldExtractExpiration() {
            // Arrange
            String token = jwtTokenProvider.generateToken(testUserDetails);

            // Act
            Date expiration = jwtTokenProvider.extractExpiration(token);

            // Assert
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()), "Expiration phải trong tương lai");
        }
    }
}
