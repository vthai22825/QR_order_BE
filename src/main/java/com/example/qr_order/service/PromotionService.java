package com.example.qr_order.service;

import com.example.qr_order.dtos.PromotionRequest;
import com.example.qr_order.dtos.response.PromotionResponse;
import com.example.qr_order.entity.Promotion;
import com.example.qr_order.enums.DiscountType;
import com.example.qr_order.repository.PromotionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepo promotionRepo;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Promotion create(PromotionRequest promotionRequest, MultipartFile file){

        //check name
        if (promotionRepo.existsByNameAndIsDeletedFalse(promotionRequest.getName().trim())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "promotion name already exist");
        }

        //check date
        if (promotionRequest.getStartDate().isBefore(Instant.now().minus(2, ChronoUnit.MINUTES))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start date must start from now");
        }

        if (promotionRequest.getEndDate().isBefore(promotionRequest.getStartDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End date cannot be before start date");
        }

        //check logic %
        if (promotionRequest.getDiscountType() == DiscountType.PERCENTAGE){
            if (promotionRequest.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot discount over 100%");
            }
        }

        Promotion promotion = new Promotion();
        promotion.setName(promotionRequest.getName().trim());

        promotion.setDescription(promotionRequest.getDescription());
        promotion.setDiscountType(promotionRequest.getDiscountType());
        promotion.setDiscountValue(promotionRequest.getDiscountValue());
        promotion.setStartDate(promotionRequest.getStartDate());
        promotion.setEndDate(promotionRequest.getEndDate());

        if (file != null && !file.isEmpty()) {
            String imageUrl = imageStorageService.uploadImageSync(file, "promotion");
            promotion.setPromotionImage(imageUrl);
        }

        return promotionRepo.save(promotion);

    }

    @Transactional
    public Promotion update(Long id, PromotionRequest request, MultipartFile file) {
        Promotion existingPromotion = promotionRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));

        // 2. CẬP NHẬT ĐỘNG: TÊN (Và check trùng tên)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!existingPromotion.getName().equalsIgnoreCase(newName)) {
                if (promotionRepo.existsByNameAndIsDeletedFalse(newName)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên chương trình khuyến mãi đã tồn tại");
                }
            }
            existingPromotion.setName(newName);
        }

        if (request.getDescription() != null) {
            existingPromotion.setDescription(request.getDescription());
        }

        // 3. CẬP NHẬT ĐỘNG: LOẠI GIẢM GIÁ & GIÁ TRỊ
        if (request.getDiscountType() != null) {
            existingPromotion.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            existingPromotion.setDiscountValue(request.getDiscountValue());
        }

        // Kiểm tra lại logic Phần trăm (Trên dữ liệu đã được gộp)
        if (existingPromotion.getDiscountType() == DiscountType.PERCENTAGE) {
            if (existingPromotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mức giảm phần trăm không được vượt quá 100%");
            }
        }

        // 4. CẬP NHẬT ĐỘNG: NGÀY THÁNG
        if (request.getStartDate() != null) {
            existingPromotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existingPromotion.setEndDate(request.getEndDate());
        }

        if (existingPromotion.getEndDate().isBefore(existingPromotion.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc không được trước ngày bắt đầu");
        }

        if (file != null && !file.isEmpty()) {
            String newImageUrl = imageStorageService.uploadImageSync(file, "promotion");
            existingPromotion.setPromotionImage(newImageUrl);

        }

        return promotionRepo.save(existingPromotion);
    }

    // 3. LẤY CHI TIẾT 1 KHUYẾN MÃI
    public Promotion getPromotionById(Long id) {
        return promotionRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));
    }

    // 4. LẤY DANH SÁCH (CÓ PHÂN TRANG & TÌM KIẾM)
    public Page<Promotion> getAllPromotions(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (name != null && !name.trim().isEmpty()) {
            return promotionRepo.findByNameContainingIgnoreCaseAndIsDeletedFalse(name.trim(), pageable);
        }

        return promotionRepo.findByIsDeletedFalse(pageable);
    }

    // 5. XÓA MỀM (SOFT DELETE)
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));

        promotion.setDeleted(true);
        promotion.setActive(false);
        promotionRepo.save(promotion);
    }

    // 6. BẬT/TẮT NHANH (TOGGLE ACTIVE)
    @Transactional
    public void toggleActiveStatus(Long id) {
        Promotion promotion = promotionRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi"));

        promotion.setActive(!promotion.isActive());
        promotionRepo.save(promotion);
    }
}
