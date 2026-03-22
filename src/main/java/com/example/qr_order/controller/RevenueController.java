package com.example.qr_order.controller;

import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.RevenueResponse;
import com.example.qr_order.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    /**
     * Thống kê doanh thu theo ngày
     * GET /api/revenue/daily?date=2025-03-22
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenueByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        RevenueResponse data = revenueService.getRevenueByDay(date);
        return ResponseEntity.ok(ApiResponse.<RevenueResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo ngày thành công")
                .data(data)
                .build());
    }

    /**
     * Thống kê doanh thu theo tháng
     * GET /api/revenue/monthly?year=2025&month=3
     */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenueByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body(ApiResponse.<RevenueResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Tháng không hợp lệ (1-12)")
                    .build());
        }
        RevenueResponse data = revenueService.getRevenueByMonth(year, month);
        return ResponseEntity.ok(ApiResponse.<RevenueResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo tháng thành công")
                .data(data)
                .build());
    }

    /**
     * Thống kê doanh thu theo năm
     * GET /api/revenue/yearly?year=2025
     */
    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenueByYear(
            @RequestParam int year
    ) {
        RevenueResponse data = revenueService.getRevenueByYear(year);
        return ResponseEntity.ok(ApiResponse.<RevenueResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo năm thành công")
                .data(data)
                .build());
    }
}
