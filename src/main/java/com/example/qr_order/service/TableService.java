package com.example.qr_order.service;

import com.example.qr_order.dtos.TableRequest;
import com.example.qr_order.dtos.response.table.TableResponse;
import com.example.qr_order.entity.Tables;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.repository.TableRepo;
import com.example.qr_order.repository.TableSessionRepo;
import com.example.qr_order.repository.SessionHistoryRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.qr_order.entity.SessionHistory;
import com.example.qr_order.entity.TableSession;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {

    private final TableRepo tableRepo;
    private final TableSessionRepo tableSessionRepo;
    private final SessionHistoryRepo sessionHistoryRepo;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    public TableService(TableRepo tableRepo, TableSessionRepo tableSessionRepo, SessionHistoryRepo sessionHistoryRepo) {
        this.tableRepo = tableRepo;
        this.tableSessionRepo = tableSessionRepo;
        this.sessionHistoryRepo = sessionHistoryRepo;
    }

    @Transactional
    public TableResponse createTable(TableRequest request) {
        Tables table = new Tables();
        table.setTableName(request.getTableName());

        // Save first to get ID
        table = tableRepo.save(table);

        // Generate QR URL
        String qrUrl = frontendBaseUrl + "/table/" + table.getTableId();
        table.setQrUrl(qrUrl);

        // Update table
        table = tableRepo.save(table);
        return mapToResponse(table);
    }

    public List<TableResponse> getAllTables() {
        return tableRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TableResponse getTableById(Long id) {
        Tables table = tableRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Table with ID " + id + " not found"));
        return mapToResponse(table);
    }

    @Transactional
    public TableResponse updateTable(Long id, TableRequest request) {
        Tables table = tableRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Table with ID " + id + " not found"));

        table.setTableName(request.getTableName());
        table = tableRepo.save(table);
        return mapToResponse(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        if (!tableRepo.existsById(id)) {
            throw new RuntimeException("Table with ID " + id + " not found");
        }
        tableRepo.deleteById(id);
    }

    @Transactional
    public TableResponse occupyTable(Long tableId) {
        Tables table = tableRepo.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table with ID " + tableId + " not found"));

        if (tableSessionRepo.findByTables(table).isPresent()) {
            throw new RuntimeException("Table is already occupied");
        }

        TableSession session = new TableSession();
        session.setTables(table);
        tableSessionRepo.save(session);

        return mapToResponse(table);
    }

    @Transactional
    public TableResponse endSession(Long tableId) {
        Tables table = tableRepo.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table with ID " + tableId + " not found"));

        TableSession session = tableSessionRepo.findByTables(table)
                .orElseThrow(() -> new RuntimeException("Table is not occupied"));

        // Archive to history
        SessionHistory history = new SessionHistory();
        history.setTableId(table.getTableId());
        history.setTableName(table.getTableName());
        history.setStartTime(session.getStartTime());
        history.setEndTime(java.time.Instant.now());
        history.setTotalAmount(session.getCurrentTotalAmount());
        sessionHistoryRepo.save(history);

        // Delete active session
        tableSessionRepo.delete(session);

        return mapToResponse(table);
    }

    private TableResponse mapToResponse(Tables table) {
        TableResponse response = new TableResponse();
        response.setTableId(table.getTableId());
        response.setTableName(table.getTableName());
        response.setQrUrl(table.getQrUrl());
        response.setCreatedAt(table.getCreatedAt());
        response.setUpdatedAt(table.getUpdatedAt());

        boolean isOccupied = tableSessionRepo.findByTables(table).isPresent();
        response.setStatus(isOccupied ? TableStatus.OCCUPIED.name() : TableStatus.EMPTY.name());

        return response;
    }
}
