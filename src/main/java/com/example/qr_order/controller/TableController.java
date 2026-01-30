package com.example.qr_order.controller;

import com.example.qr_order.dtos.TableRequest;
import com.example.qr_order.dtos.response.table.TableResponse;
import com.example.qr_order.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<TableResponse> createTable(@RequestBody TableRequest request) {
        TableResponse createdTable = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTable);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TableResponse> updateTable(@PathVariable Long id, @RequestBody TableRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/occupy")
    public ResponseEntity<TableResponse> occupyTable(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.occupyTable(id));
    }

    @PostMapping("/{id}/end-session")
    public ResponseEntity<TableResponse> endSession(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.endSession(id));
    }
}
