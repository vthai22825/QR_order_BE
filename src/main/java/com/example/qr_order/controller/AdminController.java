package com.example.qr_order.controller;

import com.example.qr_order.dtos.CreateEmployeeRequest;
import com.example.qr_order.dtos.response.MessageResponse;
import com.example.qr_order.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @PostMapping("/employees")
    public ResponseEntity<MessageResponse> createEmployee(@RequestBody CreateEmployeeRequest request) {
        MessageResponse response = authService.createEmployee(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    public ResponseEntity<java.util.List<com.example.qr_order.dtos.response.EmployeeResponse>> getEmployees() {
        return ResponseEntity.ok(authService.getAllEmployees());
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@PathVariable Long id) {
        MessageResponse response = authService.resetPassword(id);
        return ResponseEntity.ok(response);
    }
}
