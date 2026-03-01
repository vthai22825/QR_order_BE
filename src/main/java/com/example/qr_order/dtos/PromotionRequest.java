package com.example.qr_order.dtos;

import com.example.qr_order.enums.DiscountType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "promotion's name cannot be null")
    private String name;

    private String description;

    @NotNull(message = "promotion type cannot be null")
    private DiscountType discountType;

    @NotNull(message = "discount value cannot be null")
    @Min(value = 1, message = "discount value must be great than zero")
    private BigDecimal discountValue;

    @NotNull(message = "start date cannot be null")
    //@FutureOrPresent(message = "start date must start from now")
    private Instant startDate;

    @NotNull(message = "end date cannot be null")
    private Instant endDate;
}
