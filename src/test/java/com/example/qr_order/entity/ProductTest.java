package com.example.qr_order.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Product Entity.
 * 
 * Phương pháp kiểm thử áp dụng:
 * - Boundary Value Analysis (BVA): Kiểm tra giá trị biên cho price (0, -1, null)
 * - Equivalence Partitioning (EP): Phân vùng valid/invalid cho name, price
 * - White-box: Statement & Branch Coverage cho tất cả setter methods
 */
@DisplayName("Product Entity Tests")
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
    }

    // =========================================================================
    // TC_ENT_PROD_NAME: Kiểm thử setter name
    // =========================================================================
    @Nested
    @DisplayName("setName() - Kiểm thử tên sản phẩm")
    class SetNameTests {

        @Test
        @DisplayName("TC_ENT_PROD_NAME_01 - [BVA Invalid] setName(null) → IllegalArgumentException")
        void shouldThrowExceptionWhenNameIsNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> product.setName(null)
            );
            assertEquals("Product name cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("TC_ENT_PROD_NAME_02 - [EP Valid] setName('Phở bò') → Trim & set thành công")
        void shouldSetNameSuccessfully() {
            // Act
            product.setName("  Phở bò  ");

            // Assert
            assertEquals("Phở bò", product.getName());
        }

        @Test
        @DisplayName("TC_ENT_PROD_NAME_03 - [EP Valid] setName với tên bình thường")
        void shouldSetNormalNameSuccessfully() {
            // Act
            product.setName("Cơm tấm");

            // Assert
            assertEquals("Cơm tấm", product.getName());
        }
    }

    // =========================================================================
    // TC_ENT_PROD_PRICE: Kiểm thử setter price
    // =========================================================================
    @Nested
    @DisplayName("setPrice() - Kiểm thử giá sản phẩm")
    class SetPriceTests {

        @Test
        @DisplayName("TC_ENT_PROD_PRICE_01 - [BVA Boundary] setPrice(0) → Thành công (biên dưới)")
        void shouldAcceptZeroPrice() {
            // Act
            product.setPrice(BigDecimal.ZERO);

            // Assert
            assertEquals(BigDecimal.ZERO, product.getPrice());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PRICE_02 - [EP Invalid] setPrice(negative) → IllegalArgumentException")
        void shouldThrowExceptionWhenPriceIsNegative() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> product.setPrice(new BigDecimal("-1"))
            );
            assertEquals("Price cannot be nagative", exception.getMessage());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PRICE_03 - [EP Invalid] setPrice(null) → IllegalArgumentException")
        void shouldThrowExceptionWhenPriceIsNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> product.setPrice(null)
            );
            assertEquals("Price can not be null", exception.getMessage());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PRICE_04 - [EP Valid] setPrice(positive) → Thành công")
        void shouldSetPositivePriceSuccessfully() {
            // Arrange
            BigDecimal price = new BigDecimal("35000");

            // Act
            product.setPrice(price);

            // Assert
            assertEquals(price, product.getPrice());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PRICE_05 - [BVA Boundary] setPrice(1) → Thành công (biên dưới + 1)")
        void shouldAcceptMinimumPositivePrice() {
            // Act
            product.setPrice(BigDecimal.ONE);

            // Assert
            assertEquals(BigDecimal.ONE, product.getPrice());
        }
    }

    // =========================================================================
    // TC_ENT_PROD_BS: Kiểm thử setBestSeller
    // =========================================================================
    @Nested
    @DisplayName("setBestSeller() - Kiểm thử trạng thái Best Seller")
    class SetBestSellerTests {

        @Test
        @DisplayName("TC_ENT_PROD_BS_01 - [EP Valid] setBestSeller(null) → default false")
        void shouldDefaultToFalseWhenNull() {
            // Act
            product.setBestSeller(null);

            // Assert
            assertFalse(product.getIsBestSeller());
        }

        @Test
        @DisplayName("TC_ENT_PROD_BS_02 - [EP Valid] setBestSeller(true) → true")
        void shouldSetBestSellerToTrue() {
            // Act
            product.setBestSeller(true);

            // Assert
            assertTrue(product.getIsBestSeller());
        }

        @Test
        @DisplayName("TC_ENT_PROD_BS_03 - [EP Valid] setBestSeller(false) → false")
        void shouldSetBestSellerToFalse() {
            // Act
            product.setBestSeller(false);

            // Assert
            assertFalse(product.getIsBestSeller());
        }
    }

    // =========================================================================
    // TC_ENT_PROD_OTHER: Kiểm thử các setter khác
    // =========================================================================
    @Nested
    @DisplayName("Other setters - Kiểm thử các setter phụ")
    class OtherSetterTests {

        @Test
        @DisplayName("TC_ENT_PROD_DESC_01 - [EP Valid] setDescription → Thành công")
        void shouldSetDescription() {
            // Act
            product.setDescription("Món ăn ngon");

            // Assert
            assertEquals("Món ăn ngon", product.getDescription());
        }

        @Test
        @DisplayName("TC_ENT_PROD_IMG_01 - [EP Valid] setImageUrl → Thành công")
        void shouldSetImageUrl() {
            // Act
            product.setImageUrl("https://example.com/image.jpg");

            // Assert
            assertEquals("https://example.com/image.jpg", product.getImageUrl());
        }

        @Test
        @DisplayName("TC_ENT_PROD_DEL_01 - [EP Valid] setDeleted(true) → Soft delete")
        void shouldSetDeletedTrue() {
            // Act
            product.setDeleted(true);

            // Assert
            assertTrue(product.isDeleted());
        }

        @Test
        @DisplayName("TC_ENT_PROD_DEL_02 - [EP Valid] setDeleted(false) → Restore")
        void shouldSetDeletedFalse() {
            // Arrange
            product.setDeleted(true);

            // Act
            product.setDeleted(false);

            // Assert
            assertFalse(product.isDeleted());
        }

        @Test
        @DisplayName("TC_ENT_PROD_CAT_01 - [EP Valid] setCategory → Thành công")
        void shouldSetCategory() {
            // Arrange
            Category category = new Category();

            // Act
            product.setCategory(category);

            // Assert
            assertEquals(category, product.getCategory());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PROMO_01 - [EP Valid] setPromotion → Thành công")
        void shouldSetPromotion() {
            // Arrange
            Promotion promotion = new Promotion();

            // Act
            product.setPromotion(promotion);

            // Assert
            assertEquals(promotion, product.getPromotion());
        }

        @Test
        @DisplayName("TC_ENT_PROD_PROMO_02 - [EP Valid] setPromotion(null) → Gỡ promotion")
        void shouldRemovePromotion() {
            // Arrange
            product.setPromotion(new Promotion());

            // Act
            product.setPromotion(null);

            // Assert
            assertNull(product.getPromotion());
        }
    }
}
