package com.mumsaqua.controller;

import com.mumsaqua.entity.Customer;
import com.mumsaqua.entity.Sale;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SaleRepository saleRepo;
    private final CustomerRepository customerRepo;
    private final RawMaterialRepository materialRepo;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Sale> allSales = saleRepo.findAll();
        List<Customer> allCustomers = customerRepo.findAll();

        BigDecimal totalRevenue = allSales.stream()
                .map(Sale::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingBalance = allCustomers.stream()
                .map(Customer::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = materialRepo.findByCurrentStockLessThan(100).size();

        // Simplified cost (can be enhanced later)
        BigDecimal totalCost = totalRevenue.multiply(BigDecimal.valueOf(0.66)); // ~66% cost ratio placeholder
        BigDecimal netProfit = totalRevenue.subtract(totalCost);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCost", totalCost);
        stats.put("netProfit", netProfit);
        stats.put("pendingBalance", pendingBalance);
        stats.put("lowStockCount", lowStockCount);
        return stats;
    }

    @GetMapping("/low-stock")
    public List<Map<String, Object>> getLowStock() {
        return materialRepo.findByCurrentStockLessThan(100).stream().map(m -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", m.getId());
            dto.put("name", m.getName());
            dto.put("type", m.getType());
            dto.put("currentStock", m.getCurrentStock());
            dto.put("unit", m.getUnit());
            dto.put("threshold", 100);
            return dto;
        }).toList();
    }

    @GetMapping("/daily-sales")
    public List<Map<String, Object>> getDailySales() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            LocalDate date = d;
            List<Sale> daySales = saleRepo.findBySaleDateBetweenOrderBySaleDateDesc(date, date);
            BigDecimal revenue = daySales.stream()
                    .map(Sale::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")));
            entry.put("revenue", revenue);
            entry.put("invoices", daySales.size());
            result.add(entry);
        }
        return result;
    }
}
