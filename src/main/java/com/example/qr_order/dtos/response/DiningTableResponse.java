package com.example.qr_order.dtos.response;

import com.example.qr_order.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiningTableResponse {
    private Long id;
    private String name;
    private TableStatus status;
}