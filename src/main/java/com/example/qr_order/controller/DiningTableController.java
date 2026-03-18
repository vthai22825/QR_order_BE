package com.example.qr_order.controller;

import com.example.qr_order.dtos.DiningTableRequest;
import com.example.qr_order.dtos.response.ApiResponse;
import com.example.qr_order.dtos.response.DiningTableResponse;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.service.DiningTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService tableService;


    @PostMapping
    public ResponseEntity<ApiResponse<DiningTableResponse>> createTable(@Valid @RequestBody DiningTableRequest request) {
        DiningTableResponse newTable = tableService.create(request);

        ApiResponse<DiningTableResponse> response = ApiResponse.<DiningTableResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Created table successful")
                .data(newTable)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiningTableResponse>> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody DiningTableRequest request) {
        DiningTableResponse updatedTable = tableService.update(id, request);

        ApiResponse<DiningTableResponse> response = ApiResponse.<DiningTableResponse>builder()
                .status(HttpStatus.OK.value())
                .message("updated table successful")
                .data(updatedTable)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DiningTableResponse>> updateTableStatus(
            @PathVariable Long id,
            @RequestParam TableStatus status
    ) {
        DiningTableResponse updatedTable = tableService.updateStatus(id, status);

        ApiResponse<DiningTableResponse> response = ApiResponse.<DiningTableResponse>builder()
                .status(HttpStatus.OK.value())
                .message("updated table status successful")
                .data(updatedTable)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiningTableResponse>>> getAllTables(
            @RequestParam(required = false) TableStatus status
    ) {
        List<DiningTableResponse> result = tableService.getAll(status);

        ApiResponse<List<DiningTableResponse>> response = ApiResponse.<List<DiningTableResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Get all table successful")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiningTableResponse>> getTableById(@PathVariable Long id) {
        DiningTableResponse table = tableService.getById(id);

        ApiResponse<DiningTableResponse> response = ApiResponse.<DiningTableResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Get detail table successful")
                .data(table)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.delete(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted table successful")
                .build();

        return ResponseEntity.ok(response);
    }

}
