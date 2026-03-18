package com.example.qr_order.service;

import com.example.qr_order.dtos.DiningTableRequest;
import com.example.qr_order.dtos.response.DiningTableResponse;
import com.example.qr_order.entity.DiningTable;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.repository.DiningTableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiningTableService {

    private final DiningTableRepo tableRepo;

    @Transactional
    public DiningTableResponse create(DiningTableRequest request) {

        if (tableRepo.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Table's name '" + request.getName() + "' already exist");
        }

        DiningTable table = new DiningTable();
        table.setName(request.getName());

        DiningTable savedTable = tableRepo.save(table);
        return mapToResponse(savedTable);
    }

    @Transactional
    public DiningTableResponse update(Long id, DiningTableRequest request) {
        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found table"));

        if (!request.getName().equals(table.getName())) {
            if (tableRepo.existsByNameAndIsDeletedFalse(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Table's name already exist");
            }
            table.setName(request.getName());
        }

        return mapToResponse(tableRepo.save(table));
    }

    @Transactional
    public DiningTableResponse updateStatus(Long id, TableStatus newStatus) {
        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found table"));

        table.setStatus(newStatus);
        return mapToResponse(tableRepo.save(table));
    }


    public List<DiningTableResponse> getAll(TableStatus status) {

        List<DiningTable> tables;

        if (status != null) {
            tables = tableRepo.findAllByIsDeletedFalseAndStatusOrderByIdDesc(status);
        } else {
            tables = tableRepo.findAllByIsDeletedFalseOrderByIdDesc();
        }

        return tables.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DiningTableResponse getById(Long id) {
        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found table"));
        return mapToResponse(table);
    }

    @Transactional
    public void delete(Long id) {
        DiningTable table = tableRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found table"));

        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not deleted, table is occupied");
        }

        table.setDeleted(true);
        tableRepo.save(table);
    }



    private DiningTableResponse mapToResponse(DiningTable table) {
        return new DiningTableResponse(
                table.getId(),
                table.getName(),
                table.getStatus()
        );
    }
}
