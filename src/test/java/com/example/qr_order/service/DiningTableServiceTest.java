package com.example.qr_order.service;

import com.example.qr_order.dtos.DiningTableRequest;
import com.example.qr_order.dtos.response.DiningTableResponse;
import com.example.qr_order.entity.DiningTable;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.repository.DiningTableRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho DiningTableService.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Valid/Invalid cho CRUD operations
 * - Decision Table: Logic xóa bàn (AVAILABLE vs OCCUPIED)
 * - White-box: Branch coverage cho tất cả nhánh điều kiện
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiningTableService Tests")
class DiningTableServiceTest {

    @Mock
    private DiningTableRepo tableRepo;

    @InjectMocks
    private DiningTableService diningTableService;

    private DiningTableRequest validRequest;
    private DiningTable existingTable;

    @BeforeEach
    void setUp() {
        validRequest = new DiningTableRequest();
        validRequest.setName("Bàn 1");

        existingTable = new DiningTable();
        existingTable.setName("Bàn 1");
        existingTable.setStatus(TableStatus.AVAILABLE);
    }

    // =========================================================================
    // TC_SVC_TBL_CREATE: Kiểm thử create()
    // =========================================================================
    @Nested
    @DisplayName("create() - Tạo bàn mới")
    class CreateTests {

        @Test
        @DisplayName("TC_SVC_TBL_CREATE_01 - [EP Valid] Tạo bàn mới thành công")
        void shouldCreateTableSuccessfully() {
            // Arrange
            when(tableRepo.existsByNameAndIsDeletedFalse("Bàn 1")).thenReturn(false);
            when(tableRepo.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            DiningTableResponse result = diningTableService.create(validRequest);

            // Assert
            assertNotNull(result);
            verify(tableRepo).save(any(DiningTable.class));
        }

        @Test
        @DisplayName("TC_SVC_TBL_CREATE_02 - [EP Invalid] Tên bàn trùng → CONFLICT")
        void shouldThrowConflictWhenNameExists() {
            // Arrange
            when(tableRepo.existsByNameAndIsDeletedFalse("Bàn 1")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.create(validRequest));
        }
    }

    // =========================================================================
    // TC_SVC_TBL_UPDATE: Kiểm thử update()
    // =========================================================================
    @Nested
    @DisplayName("update() - Cập nhật bàn")
    class UpdateTests {

        @Test
        @DisplayName("TC_SVC_TBL_UPDATE_01 - [EP Valid] Đổi tên bàn thành công")
        void shouldUpdateTableNameSuccessfully() {
            // Arrange
            DiningTableRequest updateRequest = new DiningTableRequest();
            updateRequest.setName("Bàn VIP");

            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));
            when(tableRepo.existsByNameAndIsDeletedFalse("Bàn VIP")).thenReturn(false);
            when(tableRepo.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            DiningTableResponse result = diningTableService.update(1L, updateRequest);

            // Assert
            assertNotNull(result);
            verify(tableRepo).save(any(DiningTable.class));
        }

        @Test
        @DisplayName("TC_SVC_TBL_UPDATE_02 - [EP Invalid] Bàn không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenTableDoesNotExist() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.update(999L, validRequest));
        }

        @Test
        @DisplayName("TC_SVC_TBL_UPDATE_03 - [EP Invalid] Tên mới đã tồn tại → CONFLICT")
        void shouldThrowConflictWhenNewNameAlreadyExists() {
            // Arrange
            DiningTableRequest updateRequest = new DiningTableRequest();
            updateRequest.setName("Bàn 2");

            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));
            when(tableRepo.existsByNameAndIsDeletedFalse("Bàn 2")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.update(1L, updateRequest));
        }

        @Test
        @DisplayName("TC_SVC_TBL_UPDATE_04 - [EP Valid] Giữ nguyên tên → Không check trùng")
        void shouldSkipDuplicateCheckWhenNameUnchanged() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));
            when(tableRepo.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            DiningTableResponse result = diningTableService.update(1L, validRequest);

            // Assert
            assertNotNull(result);
            verify(tableRepo, never()).existsByNameAndIsDeletedFalse(anyString());
        }
    }

    // =========================================================================
    // TC_SVC_TBL_STATUS: Kiểm thử updateStatus()
    // =========================================================================
    @Nested
    @DisplayName("updateStatus() - Cập nhật trạng thái")
    class UpdateStatusTests {

        @Test
        @DisplayName("TC_SVC_TBL_STATUS_01 - [EP Valid] Cập nhật AVAILABLE → OCCUPIED")
        void shouldUpdateStatusToOccupied() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));
            when(tableRepo.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            DiningTableResponse result = diningTableService.updateStatus(1L, TableStatus.OCCUPIED);

            // Assert
            assertNotNull(result);
            verify(tableRepo).save(any(DiningTable.class));
        }

        @Test
        @DisplayName("TC_SVC_TBL_STATUS_02 - [EP Invalid] Bàn không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenUpdatingStatusOfNonExistentTable() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.updateStatus(999L, TableStatus.OCCUPIED));
        }
    }

    // =========================================================================
    // TC_SVC_TBL_DELETE: Kiểm thử delete()
    // =========================================================================
    @Nested
    @DisplayName("delete() - Xóa mềm bàn")
    class DeleteTests {

        @Test
        @DisplayName("TC_SVC_TBL_DELETE_01 - [EP Valid] Xóa bàn trống (AVAILABLE) → Thành công")
        void shouldDeleteAvailableTable() {
            // Arrange
            existingTable.setStatus(TableStatus.AVAILABLE);
            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));

            // Act
            diningTableService.delete(1L);

            // Assert
            verify(tableRepo).save(existingTable);
        }

        @Test
        @DisplayName("TC_SVC_TBL_DELETE_02 - [Decision Table] Xóa bàn đang OCCUPIED → BAD_REQUEST")
        void shouldThrowBadRequestWhenDeletingOccupiedTable() {
            // Arrange
            existingTable.setStatus(TableStatus.OCCUPIED);
            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.delete(1L));
        }

        @Test
        @DisplayName("TC_SVC_TBL_DELETE_03 - [EP Invalid] ID không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenDeletingNonExistentTable() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.delete(999L));
        }
    }

    // =========================================================================
    // TC_SVC_TBL_GETBYID: Kiểm thử getById()
    // =========================================================================
    @Nested
    @DisplayName("getById() - Lấy chi tiết bàn")
    class GetByIdTests {

        @Test
        @DisplayName("TC_SVC_TBL_GETBYID_01 - [EP Valid] Tìm thấy bàn → Return response")
        void shouldReturnTableById() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingTable));

            // Act
            DiningTableResponse result = diningTableService.getById(1L);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("TC_SVC_TBL_GETBYID_02 - [EP Invalid] Không tìm thấy → NOT_FOUND")
        void shouldThrowNotFoundWhenTableDoesNotExist() {
            // Arrange
            when(tableRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> diningTableService.getById(999L));
        }
    }
}
