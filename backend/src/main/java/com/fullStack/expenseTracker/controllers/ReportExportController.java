package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.models.Transaction;
import com.fullStack.expenseTracker.repository.TransactionRepository;
import com.fullStack.expenseTracker.services.ReportExportService;
import com.fullStack.expenseTracker.specifications.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/mypockit/report/export")
@CrossOrigin(origins = {"http://localhost:5000"})
@RequiredArgsConstructor
@Slf4j
public class ReportExportController {

    private final TransactionRepository transactionRepository;
    private final ReportExportService reportExportService;

    @GetMapping("/pdf")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String email,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                            @RequestParam(required = false) Integer categoryId,
                                            @RequestParam(required = false) Double min,
                                            @RequestParam(required = false) Double max) {
        try {
            Specification<Transaction> spec = TransactionSpecification.withFilters(email, from, to, categoryId, min, max);
            List<Transaction> data = transactionRepository.findAll(spec);
            byte[] pdf = reportExportService.toPdf("Transactions", data);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=transactions.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error exporting PDF: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/excel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) String email,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                              @RequestParam(required = false) Integer categoryId,
                                              @RequestParam(required = false) Double min,
                                              @RequestParam(required = false) Double max) {
        try {
            Specification<Transaction> spec = TransactionSpecification.withFilters(email, from, to, categoryId, min, max);
            List<Transaction> data = transactionRepository.findAll(spec);
            byte[] xlsx = reportExportService.toExcel("Transactions", data);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=transactions.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(xlsx);
        } catch (Exception e) {
            log.error("Error exporting Excel: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


