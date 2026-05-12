package com.mumsaqua.controller;

import com.mumsaqua.entity.*;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepo;
    private final CustomerRepository customerRepo;
    private final SaleRepository saleRepo;

    @GetMapping
    public List<Map<String, Object>> getAll(@RequestParam(required = false) Long customerId) {
        List<Payment> payments = customerId != null
                ? paymentRepo.findByCustomerIdOrderByPaymentDateDesc(customerId)
                : paymentRepo.findAllByOrderByPaymentDateDesc();
        return payments.stream().map(this::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        LocalDate paymentDate = LocalDate.parse(body.get("paymentDate").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Long saleId = body.get("saleId") != null ? Long.valueOf(body.get("saleId").toString()) : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;

        Customer customer = customerRepo.findById(customerId).orElseThrow();
        Sale sale = saleId != null ? saleRepo.findById(saleId).orElse(null) : null;

        Payment payment = Payment.builder()
                .customer(customer)
                .sale(sale)
                .paymentDate(paymentDate)
                .amount(amount)
                .note(note)
                .build();

        Payment saved = paymentRepo.save(payment);

        // Reduce outstanding
        customer.setOutstandingBalance(
                customer.getOutstandingBalance().subtract(amount).max(BigDecimal.ZERO)
        );
        customerRepo.save(customer);

        return ResponseEntity.ok(toDto(saved));
    }

    private Map<String, Object> toDto(Payment p) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", p.getId());
        dto.put("customerId", p.getCustomer().getId());
        dto.put("customerName", p.getCustomer().getName());
        dto.put("saleId", p.getSale() != null ? p.getSale().getId() : null);
        dto.put("saleDate", p.getSale() != null ? p.getSale().getSaleDate().toString() : null);
        dto.put("paymentDate", p.getPaymentDate().toString());
        dto.put("amount", p.getAmount());
        dto.put("note", p.getNote());
        return dto;
    }
}
