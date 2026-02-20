package com.example.qr_order.service;

import com.example.qr_order.dtos.DiningTableRequest;
import com.example.qr_order.dtos.response.DiningTableResponse;
import com.example.qr_order.dtos.response.PageResponse;
import com.example.qr_order.entity.DiningTable;
import com.example.qr_order.enums.TableStatus;
import com.example.qr_order.repository.DiningTableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    public PageResponse<DiningTableResponse> getAll(int page, int size) {


        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());


        Page<DiningTable> tablePage = tableRepo.findAllByIsDeletedFalse(pageable);

        List<DiningTableResponse> tableResponses = tablePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<DiningTableResponse>builder()
                .page(tablePage.getNumber())
                .size(tablePage.getSize())
                .total(tablePage.getTotalElements())
                .items(tableResponses)
                .build();
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
