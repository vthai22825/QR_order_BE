package com.example.qr_order.service;

import com.example.qr_order.dtos.response.RevenueResponse;
import com.example.qr_order.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final OrderRepo orderRepo;

    public RevenueResponse getRevenueByDay(LocalDate date) {
        BigDecimal revenue = orderRepo.sumRevenueByDay(date);
        long count = orderRepo.countOrdersByDay(date);
        return RevenueResponse.builder()
                .period(date.toString())
                .revenue(revenue)
                .orderCount(count)
                .build();
    }

    public RevenueResponse getRevenueByMonth(int year, int month) {
        BigDecimal revenue = orderRepo.sumRevenueByMonth(year, month);
        long count = orderRepo.countOrdersByMonth(year, month);
        return RevenueResponse.builder()
                .period(year + "-" + String.format("%02d", month))
                .revenue(revenue)
                .orderCount(count)
                .build();
    }

    public RevenueResponse getRevenueByYear(int year) {
        BigDecimal revenue = orderRepo.sumRevenueByYear(year);
        long count = orderRepo.countOrdersByYear(year);
        return RevenueResponse.builder()
                .period(String.valueOf(year))
                .revenue(revenue)
                .orderCount(count)
                .build();
    }
}
