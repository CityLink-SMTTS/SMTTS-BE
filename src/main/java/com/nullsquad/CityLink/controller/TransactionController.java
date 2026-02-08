package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.OfflineTransactionSyncDTO;
import com.nullsquad.CityLink.dto.TransactionDTO;
import com.nullsquad.CityLink.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/ref/{transactionRef}")
    public ResponseEntity<TransactionDTO> getTransactionByRef(@PathVariable String transactionRef) {
        return ResponseEntity.ok(transactionService.getTransactionByRef(transactionRef));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsByUserPaged(
            @PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId, pageable));
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transactionService.getRecentTransactions(userId, limit));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(start, end));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        TransactionDTO created = transactionService.createTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TransactionDTO> completeTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.completeTransaction(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TransactionDTO> cancelTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.cancelTransaction(id));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<TransactionDTO> refundTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.refundTransaction(id));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncOfflineTransactions(
            @Valid @RequestBody OfflineTransactionSyncDTO syncDTO) {
        return ResponseEntity.ok(transactionService.syncOfflineTransactions(syncDTO));
    }

    @GetMapping("/pending-sync")
    public ResponseEntity<List<TransactionDTO>> getPendingSyncTransactions() {
        return ResponseEntity.ok(transactionService.getPendingSyncTransactions());
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserTransactionStats(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getUserTransactionStats(userId));
    }

    @GetMapping("/analytics/popular-routes")
    public ResponseEntity<List<Map<String, Object>>> getMostUsedRoutes(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transactionService.getMostUsedRoutes(limit));
    }

    @GetMapping("/analytics/revenue-by-type")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByVehicleType() {
        return ResponseEntity.ok(transactionService.getRevenueByVehicleType());
    }
}
