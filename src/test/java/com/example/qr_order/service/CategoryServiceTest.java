package com.example.qr_order.service;

import com.example.qr_order.dtos.CategoryRequest;
import com.example.qr_order.dtos.response.CategoryResponse;
import com.example.qr_order.entity.Category;
import com.example.qr_order.repository.CategoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho CategoryService.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Valid/Invalid inputs cho create, update, delete
 * - Decision Table: Logic xử lý khi tên category đã tồn tại (isDeleted true/false)
 * - Boundary Value Analysis (BVA): Empty list trường hợp
 * - White-box: Branch coverage cho tất cả nhánh nghiệp vụ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest validRequest;
    private Category existingCategory;

    @BeforeEach
    void setUp() {
        validRequest = new CategoryRequest();
        validRequest.setName("Đồ uống");
        validRequest.setDescription("Các loại đồ uống");

        existingCategory = new Category();
        existingCategory.setName("Đồ uống");
        existingCategory.setDescription("Mô tả cũ");
    }

    // =========================================================================
    // TC_SVC_CAT_CREATE: Kiểm thử create()
    // =========================================================================
    @Nested
    @DisplayName("create() - Tạo danh mục mới")
    class CreateTests {

        @Test
        @DisplayName("TC_SVC_CAT_CREATE_01 - [EP Valid] Tạo category mới thành công")
        void shouldCreateNewCategorySuccessfully() {
            // Arrange
            when(categoryRepo.findByName(anyString())).thenReturn(Optional.empty());
            when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.create(validRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Đồ uống", result.getName());
            verify(categoryRepo).save(any(Category.class));
        }

        @Test
        @DisplayName("TC_SVC_CAT_CREATE_02 - [EP Invalid] Request null → BAD_REQUEST")
        void shouldThrowExceptionWhenRequestIsNull() {
            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> categoryService.create(null));
        }

        @Test
        @DisplayName("TC_SVC_CAT_CREATE_03 - [Decision Table] Tên trùng + isDeleted=true → Khôi phục")
        void shouldRestoreDeletedCategoryWithSameName() {
            // Arrange
            existingCategory.setDeleted(true);
            when(categoryRepo.findByName("Đồ uống")).thenReturn(Optional.of(existingCategory));
            when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.create(validRequest);

            // Assert
            assertFalse(result.isDeleted(), "Category phải được khôi phục (undelete)");
            verify(categoryRepo).save(existingCategory);
        }

        @Test
        @DisplayName("TC_SVC_CAT_CREATE_04 - [Decision Table] Tên trùng + isDeleted=false → RuntimeException")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            // Arrange
            existingCategory.setDeleted(false);
            when(categoryRepo.findByName("Đồ uống")).thenReturn(Optional.of(existingCategory));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> categoryService.create(validRequest));
            assertTrue(exception.getMessage().contains("already exist"));
        }
    }

    // =========================================================================
    // TC_SVC_CAT_UPDATE: Kiểm thử update()
    // =========================================================================
    @Nested
    @DisplayName("update() - Cập nhật danh mục")
    class UpdateTests {

        @Test
        @DisplayName("TC_SVC_CAT_UPDATE_01 - [EP Valid] Update với tên mới hợp lệ")
        void shouldUpdateCategorySuccessfully() {
            // Arrange
            CategoryRequest updateRequest = new CategoryRequest();
            updateRequest.setName("Tráng miệng");
            updateRequest.setDescription("Mô tả mới");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(existingCategory));
            when(categoryRepo.existsByName("Tráng miệng")).thenReturn(false);
            when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.update(1L, updateRequest);

            // Assert
            assertEquals("Tráng miệng", result.getName());
            assertEquals("Mô tả mới", result.getDescription());
        }

        @Test
        @DisplayName("TC_SVC_CAT_UPDATE_02 - [EP Invalid] ID không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenIdDoesNotExist() {
            // Arrange
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> categoryService.update(999L, validRequest));
        }

        @Test
        @DisplayName("TC_SVC_CAT_UPDATE_03 - [EP Invalid] Tên mới đã tồn tại → CONFLICT")
        void shouldThrowConflictWhenNewNameExists() {
            // Arrange
            CategoryRequest updateRequest = new CategoryRequest();
            updateRequest.setName("Tên đã tồn tại");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(existingCategory));
            when(categoryRepo.existsByName("Tên đã tồn tại")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> categoryService.update(1L, updateRequest));
        }

        @Test
        @DisplayName("TC_SVC_CAT_UPDATE_04 - [EP Valid] Update giữ nguyên tên (chỉ đổi description)")
        void shouldUpdateDescriptionWithoutChangingName() {
            // Arrange
            CategoryRequest updateRequest = new CategoryRequest();
            updateRequest.setName("Đồ uống"); // Giữ nguyên tên
            updateRequest.setDescription("Mô tả mới hơn");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(existingCategory));
            when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.update(1L, updateRequest);

            // Assert
            assertEquals("Mô tả mới hơn", result.getDescription());
            verify(categoryRepo, never()).existsByName(anyString());
        }
    }

    // =========================================================================
    // TC_SVC_CAT_DELETE: Kiểm thử delete()
    // =========================================================================
    @Nested
    @DisplayName("delete() - Xóa mềm danh mục")
    class DeleteTests {

        @Test
        @DisplayName("TC_SVC_CAT_DELETE_01 - [EP Valid] Soft delete thành công")
        void shouldSoftDeleteSuccessfully() {
            // Arrange
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(existingCategory));
            when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Category result = categoryService.delete(1L);

            // Assert
            assertTrue(result.isDeleted());
            verify(categoryRepo).save(existingCategory);
        }

        @Test
        @DisplayName("TC_SVC_CAT_DELETE_02 - [EP Invalid] ID không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenDeletingNonExistentCategory() {
            // Arrange
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> categoryService.delete(999L));
        }
    }

    // =========================================================================
    // TC_SVC_CAT_GETALL: Kiểm thử getAll()
    // =========================================================================
    @Nested
    @DisplayName("getAll() - Lấy danh sách danh mục")
    class GetAllTests {

        @Test
        @DisplayName("TC_SVC_CAT_GETALL_01 - [EP Valid] Có data → Return list non-empty")
        void shouldReturnNonEmptyList() {
            // Arrange
            when(categoryRepo.findByIsDeletedFalse()).thenReturn(List.of(existingCategory));

            // Act
            List<CategoryResponse> result = categoryService.getAll();

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals("Đồ uống", result.get(0).getName());
        }

        @Test
        @DisplayName("TC_SVC_CAT_GETALL_02 - [BVA Empty] Không có data → Empty list")
        void shouldReturnEmptyListWhenNoCategories() {
            // Arrange
            when(categoryRepo.findByIsDeletedFalse()).thenReturn(Collections.emptyList());

            // Act
            List<CategoryResponse> result = categoryService.getAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}
