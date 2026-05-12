package com.mumsaqua.controller;

import com.mumsaqua.entity.Customer;
import com.mumsaqua.entity.Payment;
import com.mumsaqua.entity.Sale;
import com.mumsaqua.repository.CustomerRepository;
import com.mumsaqua.repository.PaymentRepository;
import com.mumsaqua.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository repo;
    private final SaleRepository saleRepo;
    private final PaymentRepository paymentRepo;

    @GetMapping
    public List<Customer> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        customer.setOutstandingBalance(BigDecimal.ZERO);
        return repo.save(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer data) {
        return repo.findById(id).map(c -> {
            if (data.getName() != null) c.setName(data.getName());
            if (data.getPhone() != null) c.setPhone(data.getPhone());
            if (data.getAddress() != null) c.setAddress(data.getAddress());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/ledger")
    public List<Map<String, Object>> getLedger(@PathVariable Long id) {
        List<Sale> sales = saleRepo.findByCustomerIdOrderBySaleDateDesc(id);
        List<Payment> payments = paymentRepo.findByCustomerIdOrderByPaymentDateDesc(id);

        // Combine into ledger entries sorted by date
        List<Map<String, Object>> entries = new ArrayList<>();

        for (Sale s : sales) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", s.getSaleDate().toString());
            entry.put("type", "SALE");
            entry.put("description", "Invoice #" + s.getId());
            entry.put("debit", s.getGrandTotal());
            entry.put("credit", BigDecimal.ZERO);
            entries.add(entry);
        }

        for (Payment p : payments) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", p.getPaymentDate().toString());
            entry.put("type", "PAYMENT");
            entry.put("description", "Payment" + (p.getSale() != null ? " for Invoice #" + p.getSale().getId() : ""));
            entry.put("debit", BigDecimal.ZERO);
            entry.put("credit", p.getAmount());
            entries.add(entry);
        }

        // Sort by date ascending
        entries.sort(Comparator.comparing(e -> e.get("date").toString()));

        // Calculate running balance
        BigDecimal balance = BigDecimal.ZERO;
        for (Map<String, Object> entry : entries) {
            BigDecimal debit = (BigDecimal) entry.get("debit");
            BigDecimal credit = (BigDecimal) entry.get("credit");
            balance = balance.add(debit).subtract(credit);
            entry.put("balance", balance);
        }

        return entries;
    }
}
