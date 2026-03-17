package com.example.qr_order.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Category Entity.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Boundary Value Analysis (BVA): Kiểm tra null cho name
 * - Equivalence Partitioning (EP): Kiểm tra valid/invalid inputs
 * - White-box: Branch coverage cho tất cả setter methods
 */
@DisplayName("Category Entity Tests")
class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    // =========================================================================
    // TC_ENT_CAT_NAME: Kiểm thử setter name
    // =========================================================================
    @Nested
    @DisplayName("setName() - Kiểm thử tên danh mục")
    class SetNameTests {

        @Test
        @DisplayName("TC_ENT_CAT_NAME_01 - [BVA Invalid] setName(null) → IllegalArgumentException")
        void shouldThrowExceptionWhenNameIsNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> category.setName(null)
            );
            assertEquals("Category name cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("TC_ENT_CAT_NAME_02 - [EP Valid] setName('  Đồ uống  ') → Trim thành 'Đồ uống'")
        void shouldTrimNameCorrectly() {
            // Act
            category.setName("  Đồ uống  ");

            // Assert
            assertEquals("Đồ uống", category.getName());
        }

        @Test
        @DisplayName("TC_ENT_CAT_NAME_03 - [EP Valid] setName với tên bình thường")
        void shouldSetNormalNameSuccessfully() {
            // Act
            category.setName("Món chính");

            // Assert
            assertEquals("Món chính", category.getName());
        }
    }

    // =========================================================================
    // TC_ENT_CAT_DEL: Kiểm thử setter isDeleted
    // =========================================================================
    @Nested
    @DisplayName("setDeleted() - Kiểm thử soft delete")
    class SetDeletedTests {

        @Test
        @DisplayName("TC_ENT_CAT_DEL_01 - [EP Valid] setDeleted(true) → Soft delete")
        void shouldSetDeletedTrue() {
            // Act
            category.setDeleted(true);

            // Assert
            assertTrue(category.isDeleted());
        }

        @Test
        @DisplayName("TC_ENT_CAT_DEL_02 - [EP Valid] setDeleted(false) → Restore")
        void shouldSetDeletedFalse() {
            // Arrange
            category.setDeleted(true);

            // Act
            category.setDeleted(false);

            // Assert
            assertFalse(category.isDeleted());
        }
    }

    // =========================================================================
    // TC_ENT_CAT_DESC: Kiểm thử setter description
    // =========================================================================
    @Nested
    @DisplayName("setDescription() - Kiểm thử mô tả")
    class SetDescriptionTests {

        @Test
        @DisplayName("TC_ENT_CAT_DESC_01 - [EP Valid] setDescription → Thành công")
        void shouldSetDescription() {
            // Act
            category.setDescription("Các loại đồ uống giải khát");

            // Assert
            assertEquals("Các loại đồ uống giải khát", category.getDescription());
        }

        @Test
        @DisplayName("TC_ENT_CAT_DESC_02 - [EP Valid] setDescription(null) → Cho phép null")
        void shouldAllowNullDescription() {
            // Act
            category.setDescription(null);

            // Assert
            assertNull(category.getDescription());
        }
    }
}
