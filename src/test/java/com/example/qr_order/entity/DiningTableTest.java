package com.example.qr_order.entity;

import com.example.qr_order.enums.TableStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho DiningTable Entity.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Boundary Value Analysis (BVA): null, empty, blank cho name; null cho status
 * - Equivalence Partitioning (EP): Valid/invalid inputs
 * - White-box: Branch coverage cho setter validation
 */
@DisplayName("DiningTable Entity Tests")
class DiningTableTest {

    private DiningTable diningTable;

    @BeforeEach
    void setUp() {
        diningTable = new DiningTable();
    }

    // =========================================================================
    // TC_ENT_TBL_NAME: Kiểm thử setter name
    // =========================================================================
    @Nested
    @DisplayName("setName() - Kiểm thử tên bàn")
    class SetNameTests {

        @Test
        @DisplayName("TC_ENT_TBL_NAME_01 - [BVA Invalid] setName(null) → IllegalArgumentException")
        void shouldThrowExceptionWhenNameIsNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> diningTable.setName(null)
            );
            assertEquals("Table name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("TC_ENT_TBL_NAME_02 - [BVA Invalid] setName('') → IllegalArgumentException")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTable.setName(""));
        }

        @Test
        @DisplayName("TC_ENT_TBL_NAME_03 - [BVA Invalid] setName('   ') → IllegalArgumentException")
        void shouldThrowExceptionWhenNameIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTable.setName("   "));
        }

        @Test
        @DisplayName("TC_ENT_TBL_NAME_04 - [EP Valid] setName(' Bàn 1 ') → Trim thành 'Bàn 1'")
        void shouldTrimNameCorrectly() {
            // Act
            diningTable.setName(" Bàn 1 ");

            // Assert
            assertEquals("Bàn 1", diningTable.getName());
        }

        @Test
        @DisplayName("TC_ENT_TBL_NAME_05 - [EP Valid] setName('VIP Room') → Thành công")
        void shouldSetNormalName() {
            // Act
            diningTable.setName("VIP Room");

            // Assert
            assertEquals("VIP Room", diningTable.getName());
        }
    }

    // =========================================================================
    // TC_ENT_TBL_STATUS: Kiểm thử setter status
    // =========================================================================
    @Nested
    @DisplayName("setStatus() - Kiểm thử trạng thái bàn")
    class SetStatusTests {

        @Test
        @DisplayName("TC_ENT_TBL_STATUS_01 - [BVA Invalid] setStatus(null) → IllegalArgumentException")
        void shouldThrowExceptionWhenStatusIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTable.setStatus(null));
        }

        @Test
        @DisplayName("TC_ENT_TBL_STATUS_02 - [EP Valid] setStatus(AVAILABLE) → Thành công")
        void shouldSetStatusAvailable() {
            // Act
            diningTable.setStatus(TableStatus.AVAILABLE);

            // Assert
            assertEquals(TableStatus.AVAILABLE, diningTable.getStatus());
        }

        @Test
        @DisplayName("TC_ENT_TBL_STATUS_03 - [EP Valid] setStatus(OCCUPIED) → Thành công")
        void shouldSetStatusOccupied() {
            // Act
            diningTable.setStatus(TableStatus.OCCUPIED);

            // Assert
            assertEquals(TableStatus.OCCUPIED, diningTable.getStatus());
        }
    }

    // =========================================================================
    // TC_ENT_TBL_DEL: Kiểm thử setDeleted
    // =========================================================================
    @Nested
    @DisplayName("setDeleted() - Kiểm thử soft delete")
    class SetDeletedTests {

        @Test
        @DisplayName("TC_ENT_TBL_DEL_01 - [EP Valid] setDeleted(true) → Soft delete")
        void shouldSetDeletedTrue() {
            diningTable.setDeleted(true);
            assertTrue(diningTable.isDeleted());
        }

        @Test
        @DisplayName("TC_ENT_TBL_DEL_02 - [EP Valid] setDeleted(false) → Restore")
        void shouldSetDeletedFalse() {
            diningTable.setDeleted(true);
            diningTable.setDeleted(false);
            assertFalse(diningTable.isDeleted());
        }
    }
}
