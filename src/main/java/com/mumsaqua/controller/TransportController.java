package com.mumsaqua.controller;

import com.mumsaqua.entity.*;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransportController {

    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;
    private final FuelPriceRepository fuelPriceRepo;
    private final FuelPriceHistoryRepository fuelHistoryRepo;
    private final DailyKmEntryRepository kmEntryRepo;

    // ── Vehicles ──────────────────────────────────────────────────────────────

    @GetMapping("/vehicles")
    public List<Map<String, Object>> getVehicles() {
        return vehicleRepo.findAll().stream().map(this::vehicleDto).toList();
    }

    @PostMapping("/vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> createVehicle(@RequestBody Vehicle vehicle) {
        return vehicleDto(vehicleRepo.save(vehicle));
    }

    @PutMapping("/vehicles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable Long id, @RequestBody Vehicle data) {
        return vehicleRepo.findById(id).map(v -> {
            if (data.getName() != null) v.setName(data.getName());
            if (data.getVehicleNumber() != null) v.setVehicleNumber(data.getVehicleNumber());
            if (data.getFuelType() != null) v.setFuelType(data.getFuelType());
            if (data.getMileage() != null) v.setMileage(data.getMileage());
            if (data.getDailyRent() != null) v.setDailyRent(data.getDailyRent());
            return ResponseEntity.ok(vehicleDto(vehicleRepo.save(v)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/vehicles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Drivers ───────────────────────────────────────────────────────────────

    @GetMapping("/drivers")
    public List<Map<String, Object>> getDrivers() {
        return driverRepo.findAll().stream().map(this::driverDto).toList();
    }

    @PostMapping("/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> createDriver(@RequestBody Driver driver) {
        return driverDto(driverRepo.save(driver));
    }

    @PutMapping("/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateDriver(@PathVariable Long id, @RequestBody Driver data) {
        return driverRepo.findById(id).map(d -> {
            if (data.getName() != null) d.setName(data.getName());
            if (data.getPhone() != null) d.setPhone(data.getPhone());
            if (data.getSalary() != null) d.setSalary(data.getSalary());
            if (data.getSalaryBasis() != null) d.setSalaryBasis(data.getSalaryBasis());
            return ResponseEntity.ok(driverDto(driverRepo.save(d)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Fuel Prices ───────────────────────────────────────────────────────────

    @GetMapping("/fuel-prices")
    public List<FuelPrice> getFuelPrices() {
        return fuelPriceRepo.findAll();
    }

    @PutMapping("/fuel-prices/{fuelType}")
    @PreAuthorize("hasRole('ADMIN')")
    public FuelPrice updateFuelPrice(@PathVariable Vehicle.FuelType fuelType, @RequestBody Map<String, Object> body) {
        BigDecimal price = new BigDecimal(body.get("pricePerUnit").toString());

        FuelPrice fp = fuelPriceRepo.findById(fuelType)
                .orElse(FuelPrice.builder().fuelType(fuelType).pricePerUnit(price).build());
        fp.setPricePerUnit(price);
        fuelPriceRepo.save(fp);

        // Log history
        fuelHistoryRepo.save(FuelPriceHistory.builder()
                .fuelType(fuelType)
                .pricePerUnit(price)
                .effectiveDate(LocalDate.now())
                .note("Price updated")
                .build());

        return fp;
    }

    @GetMapping("/fuel-prices/history")
    public List<FuelPriceHistory> getFuelPriceHistory(@RequestParam(required = false) Vehicle.FuelType fuelType) {
        if (fuelType != null) {
            return fuelHistoryRepo.findByFuelTypeOrderByEffectiveDateDesc(fuelType);
        }
        return fuelHistoryRepo.findAllByOrderByEffectiveDateDesc();
    }

    // ── KM Entries ────────────────────────────────────────────────────────────

    @GetMapping("/km-entries")
    public List<Map<String, Object>> getKmEntries(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long driverId) {

        List<DailyKmEntry> entries;
        if (date != null && driverId != null) {
            entries = kmEntryRepo.findByDateAndDriverId(LocalDate.parse(date), driverId);
        } else if (date != null) {
            entries = kmEntryRepo.findByDateOrderByIdDesc(LocalDate.parse(date));
        } else if (driverId != null) {
            entries = kmEntryRepo.findByDriverIdOrderByDateDesc(driverId);
        } else {
            entries = kmEntryRepo.findAll();
        }
        return entries.stream().map(this::kmEntryDto).toList();
    }

    @PostMapping("/km-entries")
    public Map<String, Object> createKmEntry(@RequestBody Map<String, Object> body) {
        Long vehicleId = Long.valueOf(body.get("vehicleId").toString());
        Long driverId = Long.valueOf(body.get("driverId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());
        int kmStart = Integer.parseInt(body.get("kmStart").toString());
        int kmEnd = Integer.parseInt(body.get("kmEnd").toString());
        int personalKm = body.get("personalKm") != null ? Integer.parseInt(body.get("personalKm").toString()) : 0;
        BigDecimal fuelExpense = body.get("fuelExpense") != null ? new BigDecimal(body.get("fuelExpense").toString()) : BigDecimal.ZERO;
        String note = body.get("note") != null ? body.get("note").toString() : null;

        Vehicle vehicle = vehicleRepo.findById(vehicleId).orElseThrow();
        Driver driver = driverRepo.findById(driverId).orElseThrow();

        int totalKm = Math.max(0, kmEnd - kmStart);
        int companyKm = Math.max(0, totalKm - personalKm);

        // Calculate fuel cost
        BigDecimal fuelPrice = fuelPriceRepo.findById(vehicle.getFuelType())
                .map(FuelPrice::getPricePerUnit)
                .orElse(BigDecimal.valueOf(90));
        BigDecimal fuelCost = BigDecimal.valueOf(companyKm)
                .divide(BigDecimal.valueOf(vehicle.getMileage()), 4, RoundingMode.HALF_UP)
                .multiply(fuelPrice)
                .setScale(2, RoundingMode.HALF_UP);

        DailyKmEntry entry = DailyKmEntry.builder()
                .vehicle(vehicle)
                .driver(driver)
                .date(date)
                .kmStart(kmStart)
                .kmEnd(kmEnd)
                .personalKm(personalKm)
                .totalKm(totalKm)
                .companyKm(companyKm)
                .fuelCost(fuelCost)
                .fuelExpense(fuelExpense)
                .note(note)
                .build();

        return kmEntryDto(kmEntryRepo.save(entry));
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    private Map<String, Object> vehicleDto(Vehicle v) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", v.getId());
        dto.put("name", v.getName());
        dto.put("vehicleNumber", v.getVehicleNumber());
        dto.put("fuelType", v.getFuelType());
        dto.put("mileage", v.getMileage());
        dto.put("dailyRent", v.getDailyRent());
        dto.put("assignedDriverId", v.getAssignedDriver() != null ? v.getAssignedDriver().getId() : null);
        dto.put("assignedDriverName", v.getAssignedDriver() != null ? v.getAssignedDriver().getName() : null);
        return dto;
    }

    private Map<String, Object> driverDto(Driver d) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", d.getId());
        dto.put("name", d.getName());
        dto.put("phone", d.getPhone());
        dto.put("salary", d.getSalary());
        dto.put("salaryBasis", d.getSalaryBasis());
        dto.put("assignedVehicleId", d.getAssignedVehicle() != null ? d.getAssignedVehicle().getId() : null);
        dto.put("assignedVehicleName", d.getAssignedVehicle() != null ? d.getAssignedVehicle().getName() : null);
        return dto;
    }

    private Map<String, Object> kmEntryDto(DailyKmEntry e) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", e.getId());
        dto.put("vehicleId", e.getVehicle().getId());
        dto.put("vehicleName", e.getVehicle().getName());
        dto.put("driverId", e.getDriver().getId());
        dto.put("driverName", e.getDriver().getName());
        dto.put("date", e.getDate().toString());
        dto.put("kmStart", e.getKmStart());
        dto.put("kmEnd", e.getKmEnd());
        dto.put("personalKm", e.getPersonalKm());
        dto.put("totalKm", e.getTotalKm());
        dto.put("companyKm", e.getCompanyKm());
        dto.put("fuelCost", e.getFuelCost());
        dto.put("fuelExpense", e.getFuelExpense());
        dto.put("note", e.getNote());
        return dto;
    }
}
