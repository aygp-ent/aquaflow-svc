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
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleRepository saleRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final PaymentRepository paymentRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return saleRepo.findAllByOrderBySaleDateDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return saleRepo.findById(id)
                .map(s -> ResponseEntity.ok(toDto(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        LocalDate saleDate = LocalDate.parse(body.get("saleDate").toString());

        Customer customer = customerRepo.findById(customerId).orElseThrow();

        Sale sale = Sale.builder()
                .saleDate(saleDate)
                .customer(customer)
                .grandTotal(BigDecimal.ZERO)
                .build();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map<String, Object> itemData : items) {
            Long productId = Long.valueOf(itemData.get("productId").toString());
            int pattiQty = Integer.parseInt(itemData.get("pattiQty").toString());
            BigDecimal pricePerPatti = new BigDecimal(itemData.get("pricePerPatti").toString());

            Product product = productRepo.findById(productId).orElseThrow();
            int bottles = pattiQty * product.getBottlesPerPatti();
            BigDecimal total = pricePerPatti.multiply(BigDecimal.valueOf(pattiQty));

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .pattiQty(pattiQty)
                    .pricePerPatti(pricePerPatti)
                    .bottles(bottles)
                    .total(total)
                    .build();

            sale.getItems().add(item);
            grandTotal = grandTotal.add(total);
        }

        sale.setGrandTotal(grandTotal);
        Sale saved = saleRepo.save(sale);

        // Update customer outstanding
        customer.setOutstandingBalance(customer.getOutstandingBalance().add(grandTotal));
        customerRepo.save(customer);

        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        saleRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Sales with payment status (for payment screen) ────────────────────────

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingSales(@RequestParam Long customerId) {
        return getSalesWithStatus(customerId).stream()
                .filter(s -> !"PAID".equals(s.get("status")))
                .toList();
    }

    @GetMapping("/status")
    public List<Map<String, Object>> getAllSalesWithStatus(@RequestParam Long customerId) {
        return getSalesWithStatus(customerId);
    }

    private List<Map<String, Object>> getSalesWithStatus(Long customerId) {
        List<Sale> customerSales = saleRepo.findByCustomerIdOrderBySaleDateDesc(customerId);
        return customerSales.stream().map(sale -> {
            BigDecimal paidAmount = paymentRepo.findBySaleId(sale.getId()).stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal dueAmount = sale.getGrandTotal().subtract(paidAmount).max(BigDecimal.ZERO);

            String status;
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) status = "UNPAID";
            else if (paidAmount.compareTo(sale.getGrandTotal()) >= 0) status = "PAID";
            else status = "PARTIAL";

            Map<String, Object> dto = toDto(sale);
            dto.put("paidAmount", paidAmount);
            dto.put("dueAmount", dueAmount);
            dto.put("status", status);
            return dto;
        }).toList();
    }

    private Map<String, Object> toDto(Sale sale) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", sale.getId());
        dto.put("saleDate", sale.getSaleDate().toString());
        dto.put("customerId", sale.getCustomer().getId());
        dto.put("customerName", sale.getCustomer().getName());
        dto.put("grandTotal", sale.getGrandTotal());
        dto.put("items", sale.getItems().stream().map(item -> {
            Map<String, Object> i = new LinkedHashMap<>();
            i.put("productId", item.getProduct().getId());
            i.put("productName", item.getProduct().getName());
            i.put("bottlesPerPatti", item.getProduct().getBottlesPerPatti());
            i.put("pattiQty", item.getPattiQty());
            i.put("pricePerPatti", item.getPricePerPatti());
            i.put("bottles", item.getBottles());
            i.put("total", item.getTotal());
            return i;
        }).toList());
        return dto;
    }
}
