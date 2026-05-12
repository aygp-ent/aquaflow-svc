package com.mumsaqua.controller;

import com.mumsaqua.entity.*;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final SaleRepository saleRepo;
    private final PaymentRepository paymentRepo;
    private final StockEntryRepository stockEntryRepo;
    private final LabourRepository labourRepo;
    private final AttendanceRepository attendanceRepo;
    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;
    private final DailyKmEntryRepository kmEntryRepo;
    private final RawMaterialRepository materialRepo;

    @GetMapping("/months")
    public List<Map<String, String>> getAvailableMonths() {
        // Return last 6 months
        List<Map<String, String>> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

        for (int i = 0; i < 6; i++) {
            YearMonth ym = current.minusMonths(i);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("value", ym.toString()); // e.g. "2026-05"
            m.put("label", ym.atDay(1).format(labelFmt)); // e.g. "May 2026"
            months.add(m);
        }
        return months;
    }

    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyReport(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();

        // ── Sales ─────────────────────────────────────────────────────────────
        List<Sale> monthSales = saleRepo.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
        BigDecimal totalRevenue = monthSales.stream()
                .map(Sale::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalInvoices = monthSales.size();

        // ── Payments collected this month ─────────────────────────────────────
        List<Payment> allPayments = paymentRepo.findAll().stream()
                .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
                .toList();
        BigDecimal collectedAmount = allPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingAmount = totalRevenue.subtract(collectedAmount).max(BigDecimal.ZERO);

        // ── Product sales breakdown ───────────────────────────────────────────
        Map<Long, Map<String, Object>> productMap = new LinkedHashMap<>();
        int totalBottlesSold = 0;
        int totalPattiSold = 0;

        for (Sale sale : monthSales) {
            for (SaleItem item : sale.getItems()) {
                Long pid = item.getProduct().getId();
                Map<String, Object> p = productMap.computeIfAbsent(pid, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", pid);
                    m.put("productName", item.getProduct().getName());
                    m.put("totalPatti", 0);
                    m.put("totalBottles", 0);
                    m.put("totalRevenue", BigDecimal.ZERO);
                    m.put("priceSum", BigDecimal.ZERO);
                    m.put("invoiceCount", 0);
                    return m;
                });
                p.put("totalPatti", (int) p.get("totalPatti") + item.getPattiQty());
                p.put("totalBottles", (int) p.get("totalBottles") + item.getBottles());
                p.put("totalRevenue", ((BigDecimal) p.get("totalRevenue")).add(item.getTotal()));
                p.put("priceSum", ((BigDecimal) p.get("priceSum")).add(item.getPricePerPatti()));
                p.put("invoiceCount", (int) p.get("invoiceCount") + 1);
                totalBottlesSold += item.getBottles();
                totalPattiSold += item.getPattiQty();
            }
        }

        List<Map<String, Object>> productSales = productMap.values().stream().map(p -> {
            Map<String, Object> ps = new LinkedHashMap<>();
            ps.put("productId", p.get("productId"));
            ps.put("productName", p.get("productName"));
            ps.put("totalPatti", p.get("totalPatti"));
            ps.put("totalBottles", p.get("totalBottles"));
            ps.put("totalRevenue", p.get("totalRevenue"));
            int count = (int) p.get("invoiceCount");
            BigDecimal avg = count > 0
                    ? ((BigDecimal) p.get("priceSum")).divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            ps.put("avgPricePerPatti", avg);
            ps.put("invoiceCount", count);
            return ps;
        }).sorted(Comparator.comparing(m -> (BigDecimal) m.get("totalRevenue"), Comparator.reverseOrder()))
                .toList();

        // ── Material purchases ────────────────────────────────────────────────
        List<StockEntry> monthStockEntries = stockEntryRepo.findAll().stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .toList();

        Map<Long, Map<String, Object>> matMap = new LinkedHashMap<>();
        for (StockEntry entry : monthStockEntries) {
            Long mid = entry.getMaterial().getId();
            Map<String, Object> m = matMap.computeIfAbsent(mid, k -> {
                RawMaterial mat = entry.getMaterial();
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("materialId", mid);
                mm.put("materialName", mat.getName());
                mm.put("materialType", mat.getType().name());
                mm.put("unit", mat.getUnit().name());
                mm.put("totalQuantity", 0);
                mm.put("totalCost", BigDecimal.ZERO);
                mm.put("purchaseCount", 0);
                return mm;
            });
            m.put("totalQuantity", (int) m.get("totalQuantity") + entry.getQuantity());
            m.put("totalCost", ((BigDecimal) m.get("totalCost")).add(entry.getTotalCost()));
            m.put("purchaseCount", (int) m.get("purchaseCount") + 1);
        }

        List<Map<String, Object>> materialPurchases = new ArrayList<>(matMap.values());
        materialPurchases.sort(Comparator.comparing(m -> (BigDecimal) m.get("totalCost"), Comparator.reverseOrder()));

        BigDecimal materialPurchaseCost = materialPurchases.stream()
                .map(m -> (BigDecimal) m.get("totalCost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Labour cost ───────────────────────────────────────────────────────
        List<Labour> allLabour = labourRepo.findAll();
        List<Attendance> monthAttendance = attendanceRepo.findAll().stream()
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .toList();

        List<Map<String, Object>> labourCosts = allLabour.stream().map(l -> {
            long absentDays = monthAttendance.stream()
                    .filter(a -> a.getLabour().getId().equals(l.getId()) && !a.getPresent())
                    .count();
            int presentDays = daysInMonth - (int) absentDays;
            BigDecimal dailyRate = l.getMonthlySalary().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            BigDecimal deduction = dailyRate.multiply(BigDecimal.valueOf(absentDays));
            BigDecimal effectiveCost = l.getMonthlySalary().subtract(deduction);

            Map<String, Object> lc = new LinkedHashMap<>();
            lc.put("labourId", l.getId());
            lc.put("labourName", l.getName());
            lc.put("monthlySalary", l.getMonthlySalary());
            lc.put("presentDays", presentDays);
            lc.put("absentDays", (int) absentDays);
            lc.put("effectiveCost", effectiveCost.setScale(0, RoundingMode.HALF_UP));
            lc.put("deduction", deduction.setScale(0, RoundingMode.HALF_UP));
            return lc;
        }).toList();

        BigDecimal labourCost = labourCosts.stream()
                .map(l -> (BigDecimal) l.get("effectiveCost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Transport cost ────────────────────────────────────────────────────
        List<DailyKmEntry> monthKmEntries = kmEntryRepo.findAll().stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .toList();

        BigDecimal totalDieselCost = monthKmEntries.stream()
                .map(DailyKmEntry::getFuelExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Vehicle> allVehicles = vehicleRepo.findAll();
        List<Driver> allDrivers = driverRepo.findAll();

        BigDecimal totalRent = allVehicles.stream()
                .map(Vehicle::getDailyRent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(daysInMonth));

        BigDecimal totalDriverWage = allDrivers.stream()
                .map(d -> d.getSalaryBasis() == Driver.SalaryBasis.DAILY
                        ? d.getSalary().multiply(BigDecimal.valueOf(30))
                        : d.getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransportCost = totalDieselCost.add(totalRent).add(totalDriverWage);

        Map<String, Object> transportCosts = new LinkedHashMap<>();
        transportCosts.put("totalDieselCost", totalDieselCost);
        transportCosts.put("totalRent", totalRent);
        transportCosts.put("totalDriverWage", totalDriverWage);
        transportCosts.put("totalTransportCost", totalTransportCost);
        transportCosts.put("tripCount", monthKmEntries.size());

        // ── P&L ──────────────────────────────────────────────────────────────
        BigDecimal totalCost = materialPurchaseCost.add(labourCost).add(totalTransportCost);
        BigDecimal grossProfit = totalRevenue.subtract(materialPurchaseCost);
        BigDecimal netProfit = totalRevenue.subtract(totalCost);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── Build response ────────────────────────────────────────────────────
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", month);
        report.put("monthLabel", start.format(labelFmt));
        report.put("totalRevenue", totalRevenue);
        report.put("totalInvoices", totalInvoices);
        report.put("totalBottlesSold", totalBottlesSold);
        report.put("totalPattiSold", totalPattiSold);
        report.put("collectedAmount", collectedAmount);
        report.put("pendingAmount", pendingAmount);
        report.put("materialPurchaseCost", materialPurchaseCost);
        report.put("labourCost", labourCost);
        report.put("transportCost", totalTransportCost);
        report.put("totalCost", totalCost);
        report.put("grossProfit", grossProfit);
        report.put("netProfit", netProfit);
        report.put("profitMargin", profitMargin);
        report.put("productSales", productSales);
        report.put("materialPurchases", materialPurchases);
        report.put("labourCosts", labourCosts);
        report.put("transportCosts", transportCosts);

        return report;
    }
}
