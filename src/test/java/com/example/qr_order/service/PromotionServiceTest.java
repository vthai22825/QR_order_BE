package com.example.qr_order.service;

import com.example.qr_order.dtos.PromotionRequest;
import com.example.qr_order.entity.Promotion;
import com.example.qr_order.enums.DiscountType;
import com.example.qr_order.repository.PromotionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho PromotionService.
 *
 * Phương pháp kiểm thử áp dụng:
 * - Equivalence Partitioning (EP): Valid/Invalid cho create, update, delete, toggle
 * - Boundary Value Analysis (BVA): Kiểm tra biên cho ngày tháng, phần trăm giảm giá
 * - Decision Table: Logic kiểm tra PERCENTAGE > 100%
 * - White-box: Branch coverage cho tất cả nhánh validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionService Tests")
class PromotionServiceTest {

    @Mock
    private PromotionRepo promotionRepo;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private PromotionService promotionService;

    private PromotionRequest validRequest;
    private Promotion existingPromotion;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        validRequest = new PromotionRequest();
        validRequest.setName("Giảm giá Tết");
        validRequest.setDescription("Chương trình giảm giá Tết Nguyên Đán");
        validRequest.setDiscountType(DiscountType.PERCENTAGE);
        validRequest.setDiscountValue(new BigDecimal("20"));
        validRequest.setStartDate(now.plus(1, ChronoUnit.HOURS));
        validRequest.setEndDate(now.plus(7, ChronoUnit.DAYS));

        existingPromotion = new Promotion();
        existingPromotion.setName("Giảm giá Tết");
        existingPromotion.setDiscountType(DiscountType.PERCENTAGE);
        existingPromotion.setDiscountValue(new BigDecimal("20"));
        existingPromotion.setStartDate(now.minus(1, ChronoUnit.DAYS));
        existingPromotion.setEndDate(now.plus(7, ChronoUnit.DAYS));
        existingPromotion.setActive(true);
        existingPromotion.setDeleted(false);
    }

    // =========================================================================
    // TC_SVC_PROMO_CREATE: Kiểm thử create()
    // =========================================================================
    @Nested
    @DisplayName("create() - Tạo khuyến mãi mới")
    class CreateTests {

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_01 - [EP Valid] Tạo promotion PERCENTAGE → Thành công")
        void shouldCreatePromotionSuccessfully() {
            // Arrange
            when(promotionRepo.existsByNameAndIsDeletedFalse(anyString())).thenReturn(false);
            when(promotionRepo.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Promotion result = promotionService.create(validRequest, null);

            // Assert
            assertNotNull(result);
            verify(promotionRepo).save(any(Promotion.class));
        }

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_02 - [EP Invalid] Tên trùng → CONFLICT")
        void shouldThrowConflictWhenNameExists() {
            // Arrange
            when(promotionRepo.existsByNameAndIsDeletedFalse("Giảm giá Tết")).thenReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.create(validRequest, null));
        }

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_03 - [BVA Invalid] startDate trong quá khứ → BAD_REQUEST")
        void shouldThrowExceptionWhenStartDateInPast() {
            // Arrange
            validRequest.setStartDate(Instant.now().minus(1, ChronoUnit.DAYS));
            when(promotionRepo.existsByNameAndIsDeletedFalse(anyString())).thenReturn(false);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.create(validRequest, null));
        }

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_04 - [BVA Invalid] endDate < startDate → BAD_REQUEST")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            // Arrange
            Instant futureDate = Instant.now().plus(5, ChronoUnit.DAYS);
            validRequest.setStartDate(futureDate);
            validRequest.setEndDate(futureDate.minus(1, ChronoUnit.DAYS));
            when(promotionRepo.existsByNameAndIsDeletedFalse(anyString())).thenReturn(false);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.create(validRequest, null));
        }

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_05 - [BVA Invalid] PERCENTAGE > 100% → BAD_REQUEST")
        void shouldThrowExceptionWhenPercentageExceeds100() {
            // Arrange
            validRequest.setDiscountValue(new BigDecimal("150"));
            when(promotionRepo.existsByNameAndIsDeletedFalse(anyString())).thenReturn(false);

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.create(validRequest, null));
        }

        @Test
        @DisplayName("TC_SVC_PROMO_CREATE_06 - [EP Valid] Tạo promotion FIXED_AMOUNT → Thành công")
        void shouldCreateFixedAmountPromotionSuccessfully() {
            // Arrange
            validRequest.setDiscountType(DiscountType.FIXED_AMOUNT);
            validRequest.setDiscountValue(new BigDecimal("5000"));
            when(promotionRepo.existsByNameAndIsDeletedFalse(anyString())).thenReturn(false);
            when(promotionRepo.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Promotion result = promotionService.create(validRequest, null);

            // Assert
            assertNotNull(result);
        }
    }

    // =========================================================================
    // TC_SVC_PROMO_DELETE: Kiểm thử deletePromotion()
    // =========================================================================
    @Nested
    @DisplayName("deletePromotion() - Xóa mềm khuyến mãi")
    class DeleteTests {

        @Test
        @DisplayName("TC_SVC_PROMO_DELETE_01 - [EP Valid] Soft delete → isDeleted=true, isActive=false")
        void shouldSoftDeletePromotion() {
            // Arrange
            when(promotionRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingPromotion));

            // Act
            promotionService.deletePromotion(1L);

            // Assert
            assertTrue(existingPromotion.isDeleted());
            assertFalse(existingPromotion.isActive());
            verify(promotionRepo).save(existingPromotion);
        }

        @Test
        @DisplayName("TC_SVC_PROMO_DELETE_02 - [EP Invalid] Promotion không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenPromotionDoesNotExist() {
            // Arrange
            when(promotionRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.deletePromotion(999L));
        }
    }

    // =========================================================================
    // TC_SVC_PROMO_TOGGLE: Kiểm thử toggleActiveStatus()
    // =========================================================================
    @Nested
    @DisplayName("toggleActiveStatus() - Bật/tắt trạng thái active")
    class ToggleTests {

        @Test
        @DisplayName("TC_SVC_PROMO_TOGGLE_01 - [EP Valid] Toggle active → đổi trạng thái")
        void shouldToggleActiveStatus() {
            // Arrange
            boolean originalStatus = existingPromotion.isActive();
            when(promotionRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingPromotion));

            // Act
            promotionService.toggleActiveStatus(1L);

            // Assert
            assertNotEquals(originalStatus, existingPromotion.isActive());
            verify(promotionRepo).save(existingPromotion);
        }

        @Test
        @DisplayName("TC_SVC_PROMO_TOGGLE_02 - [EP Invalid] Promotion không tồn tại → NOT_FOUND")
        void shouldThrowNotFoundWhenTogglingNonExistentPromotion() {
            // Arrange
            when(promotionRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.toggleActiveStatus(999L));
        }
    }

    // =========================================================================
    // TC_SVC_PROMO_GETBYID: Kiểm thử getPromotionById()
    // =========================================================================
    @Nested
    @DisplayName("getPromotionById() - Lấy chi tiết khuyến mãi")
    class GetByIdTests {

        @Test
        @DisplayName("TC_SVC_PROMO_GETBYID_01 - [EP Valid] Tìm thấy → Return promotion")
        void shouldReturnPromotionById() {
            // Arrange
            when(promotionRepo.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existingPromotion));

            // Act
            Promotion result = promotionService.getPromotionById(1L);

            // Assert
            assertNotNull(result);
            assertEquals("Giảm giá Tết", result.getName());
        }

        @Test
        @DisplayName("TC_SVC_PROMO_GETBYID_02 - [EP Invalid] Không tìm thấy → NOT_FOUND")
        void shouldThrowNotFoundWhenPromotionDoesNotExist() {
            // Arrange
            when(promotionRepo.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> promotionService.getPromotionById(999L));
        }
    }
}
