package com.example.qr_order.entity;

import com.example.qr_order.enums.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Promotion Entity.
 *
 * Phương pháp kiểm thử áp dụng:
 * - MC/DC (Modified Condition/Decision Coverage): Cho method isValid() có nhiều điều kiện
 * - Boundary Value Analysis (BVA): Kiểm tra biên cho discountValue, date
 * - Equivalence Partitioning (EP): Phân vùng valid/invalid
 * - Decision Table: Bảng quyết định cho isValid()
 *
 * Decision Table cho isValid():
 * | isDeleted | isActive | before startDate | after endDate | Result |
 * |-----------|----------|------------------|---------------|--------|
 * | true      | any      | any              | any           | false  |
 * | false     | false    | any              | any           | false  |
 * | false     | true     | true             | any           | false  |
 * | false     | true     | false            | true          | false  |
 * | false     | true     | false            | false         | true   |
 */
@DisplayName("Promotion Entity Tests")
class PromotionTest {

    private Promotion promotion;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
        // Setup promotion cơ bản (valid state)
        promotion.setName("Khuyến mãi Tết");
        promotion.setDiscountType(DiscountType.PERCENTAGE);
        promotion.setDiscountValue(new BigDecimal("20"));
        Instant now = Instant.now();
        promotion.setStartDate(now.minus(1, ChronoUnit.DAYS));
        promotion.setEndDate(now.plus(7, ChronoUnit.DAYS));
        promotion.setActive(true);
        promotion.setDeleted(false);
    }

    // =========================================================================
    // TC_ENT_PROMO_VALID: MC/DC Coverage cho isValid()
    // =========================================================================
    @Nested
    @DisplayName("isValid() - MC/DC Coverage")
    class IsValidTests {

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_01 - [MC/DC] active, not deleted, in range → true")
        void shouldBeValidWhenAllConditionsMet() {
            // Act
            boolean result = promotion.isValid(Instant.now());

            // Assert
            assertTrue(result, "Promotion hợp lệ phải return true");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_02 - [MC/DC] isDeleted=true → false (chỉ thay đổi condition 1)")
        void shouldBeInvalidWhenDeleted() {
            // Arrange
            promotion.setDeleted(true);

            // Act
            boolean result = promotion.isValid(Instant.now());

            // Assert
            assertFalse(result, "Promotion đã xóa phải return false");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_03 - [MC/DC] isActive=false → false (chỉ thay đổi condition 2)")
        void shouldBeInvalidWhenInactive() {
            // Arrange
            promotion.setActive(false);

            // Act
            boolean result = promotion.isValid(Instant.now());

            // Assert
            assertFalse(result, "Promotion không active phải return false");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_04 - [MC/DC] timeToCheck before startDate → false")
        void shouldBeInvalidWhenBeforeStartDate() {
            // Act - kiểm tra với thời gian trước ngày bắt đầu
            boolean result = promotion.isValid(Instant.now().minus(10, ChronoUnit.DAYS));

            // Assert
            assertFalse(result, "Thời gian trước ngày bắt đầu phải return false");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_05 - [MC/DC] timeToCheck after endDate → false")
        void shouldBeInvalidWhenAfterEndDate() {
            // Act - kiểm tra với thời gian sau ngày kết thúc
            boolean result = promotion.isValid(Instant.now().plus(30, ChronoUnit.DAYS));

            // Assert
            assertFalse(result, "Thời gian sau ngày kết thúc phải return false");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_06 - [BVA] timeToCheck = startDate (biên dưới) → true")
        void shouldBeValidAtExactStartDate() {
            // Arrange
            Instant startDate = promotion.getStartDate();

            // Act
            boolean result = promotion.isValid(startDate);

            // Assert
            assertTrue(result, "Đúng ngày bắt đầu phải return true");
        }

        @Test
        @DisplayName("TC_ENT_PROMO_VALID_07 - [BVA] timeToCheck = endDate (biên trên) → true")
        void shouldBeValidAtExactEndDate() {
            // Arrange
            Instant endDate = promotion.getEndDate();

            // Act
            boolean result = promotion.isValid(endDate);

            // Assert
            assertTrue(result, "Đúng ngày kết thúc phải return true");
        }
    }

    // =========================================================================
    // TC_ENT_PROMO_DISC: Kiểm thử discountValue
    // =========================================================================
    @Nested
    @DisplayName("setDiscountValue() - Kiểm thử giá trị giảm giá")
    class SetDiscountValueTests {

        @Test
        @DisplayName("TC_ENT_PROMO_DISC_01 - [BVA Invalid] setDiscountValue(0) → IllegalArgumentException")
        void shouldThrowExceptionWhenDiscountValueIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> promotion.setDiscountValue(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("TC_ENT_PROMO_DISC_02 - [BVA Invalid] setDiscountValue(-1) → IllegalArgumentException")
        void shouldThrowExceptionWhenDiscountValueIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> promotion.setDiscountValue(new BigDecimal("-1")));
        }

        @Test
        @DisplayName("TC_ENT_PROMO_DISC_03 - [BVA Valid] setDiscountValue(1) → Thành công (biên dưới hợp lệ)")
        void shouldAcceptSmallestValidDiscount() {
            // Act
            promotion.setDiscountValue(BigDecimal.ONE);

            // Assert
            assertEquals(BigDecimal.ONE, promotion.getDiscountValue());
        }

        @Test
        @DisplayName("TC_ENT_PROMO_DISC_04 - [EP Valid] setDiscountValue(50) → Thành công")
        void shouldAcceptNormalDiscount() {
            // Act
            promotion.setDiscountValue(new BigDecimal("50"));

            // Assert
            assertEquals(new BigDecimal("50"), promotion.getDiscountValue());
        }
    }

    // =========================================================================
    // TC_ENT_PROMO_DATE: Kiểm thử ngày tháng
    // =========================================================================
    @Nested
    @DisplayName("setEndDate() - Kiểm thử ngày kết thúc")
    class SetEndDateTests {

        @Test
        @DisplayName("TC_ENT_PROMO_DATE_01 - [EP Invalid] endDate < startDate → RuntimeException")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            // Arrange
            Instant startDate = Instant.now();
            promotion.setStartDate(startDate);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> promotion.setEndDate(startDate.minus(1, ChronoUnit.DAYS)));
        }

        @Test
        @DisplayName("TC_ENT_PROMO_DATE_02 - [BVA Valid] endDate = startDate → Hợp lệ")
        void shouldAcceptEndDateEqualsStartDate() {
            // Arrange
            Instant startDate = Instant.now();
            promotion.setStartDate(startDate);

            // Act — endDate bằng startDate: không vi phạm isBefore
            promotion.setEndDate(startDate);

            // Assert
            assertEquals(startDate, promotion.getEndDate());
        }
    }

    // =========================================================================
    // TC_ENT_PROMO_OTHER: Kiểm thử các setter khác
    // =========================================================================
    @Nested
    @DisplayName("Other setters")
    class OtherSetterTests {

        @Test
        @DisplayName("TC_ENT_PROMO_NAME_01 - setName → Thành công")
        void shouldSetName() {
            promotion.setName("Giảm giá mùa hè");
            assertEquals("Giảm giá mùa hè", promotion.getName());
        }

        @Test
        @DisplayName("TC_ENT_PROMO_DESC_01 - setDescription → Thành công")
        void shouldSetDescription() {
            promotion.setDescription("Giảm 20%");
            assertEquals("Giảm 20%", promotion.getDescription());
        }

        @Test
        @DisplayName("TC_ENT_PROMO_TYPE_01 - setDiscountType → PERCENTAGE")
        void shouldSetDiscountTypePercentage() {
            promotion.setDiscountType(DiscountType.PERCENTAGE);
            assertEquals(DiscountType.PERCENTAGE, promotion.getDiscountType());
        }

        @Test
        @DisplayName("TC_ENT_PROMO_TYPE_02 - setDiscountType → FIXED_AMOUNT")
        void shouldSetDiscountTypeFixedAmount() {
            promotion.setDiscountType(DiscountType.FIXED_AMOUNT);
            assertEquals(DiscountType.FIXED_AMOUNT, promotion.getDiscountType());
        }

        @Test
        @DisplayName("TC_ENT_PROMO_IMG_01 - setPromotionImage → Thành công")
        void shouldSetPromotionImage() {
            promotion.setPromotionImage("https://example.com/promo.jpg");
            assertEquals("https://example.com/promo.jpg", promotion.getPromotionImage());
        }
    }
}
