package com.example.qr_order.dtos.response.table;

import lombok.Data;
import java.time.Instant;

@Data
public class TableResponse {
    private Long tableId;
    private String tableName;
    private String qrUrl;
    private String status; // EMPTY, OCCUPIED, etc.
    private Instant createdAt;
    private Instant updatedAt;
}
