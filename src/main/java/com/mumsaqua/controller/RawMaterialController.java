package com.mumsaqua.controller;

import com.mumsaqua.entity.RawMaterial;
import com.mumsaqua.entity.StockEntry;
import com.mumsaqua.repository.RawMaterialRepository;
import com.mumsaqua.repository.StockEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RawMaterialController {

    private final RawMaterialRepository materialRepo;
    private final StockEntryRepository stockEntryRepo;

    @GetMapping
    public List<RawMaterial> getAll() {
        return materialRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RawMaterial> getById(@PathVariable Long id) {
        return materialRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RawMaterial create(@RequestBody RawMaterial material) {
        return materialRepo.save(material);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RawMaterial> update(@PathVariable Long id, @RequestBody RawMaterial data) {
        return materialRepo.findById(id).map(m -> {
            if (data.getName() != null) m.setName(data.getName());
            if (data.getType() != null) m.setType(data.getType());
            if (data.getBottleSize() != null) m.setBottleSize(data.getBottleSize());
            if (data.getUnit() != null) m.setUnit(data.getUnit());
            if (data.getCurrentStock() != null) m.setCurrentStock(data.getCurrentStock());
            if (data.getCostPerUnit() != null) m.setCostPerUnit(data.getCostPerUnit());
            return ResponseEntity.ok(materialRepo.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Stock Entries ─────────────────────────────────────────────────────────

    @GetMapping("/stock-entries")
    public List<Map<String, Object>> getStockEntries(@RequestParam(required = false) Long materialId) {
        List<StockEntry> entries = materialId != null
                ? stockEntryRepo.findByMaterialIdOrderByDateDesc(materialId)
                : stockEntryRepo.findAll();
        return entries.stream().map(this::stockEntryDto).toList();
    }

    @PostMapping("/stock-entries")
    public Map<String, Object> addStock(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        BigDecimal costPerUnit = new BigDecimal(body.get("costPerUnit").toString());
        BigDecimal transportCost = body.get("transportCost") != null ? new BigDecimal(body.get("transportCost").toString()) : BigDecimal.ZERO;
        BigDecimal miscCost = body.get("miscCost") != null ? new BigDecimal(body.get("miscCost").toString()) : BigDecimal.ZERO;
        String supplier = body.get("supplier") != null ? body.get("supplier").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;

        RawMaterial material = materialRepo.findById(materialId).orElseThrow();

        BigDecimal materialCost = costPerUnit.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalCost = materialCost.add(transportCost).add(miscCost);

        StockEntry entry = StockEntry.builder()
                .material(material)
                .date(date)
                .quantity(quantity)
                .costPerUnit(costPerUnit)
                .transportCost(transportCost)
                .miscCost(miscCost)
                .totalCost(totalCost)
                .supplier(supplier)
                .note(note)
                .build();

        StockEntry saved = stockEntryRepo.save(entry);

        // Update material stock and cost
        material.setCurrentStock(material.getCurrentStock() + quantity);
        material.setCostPerUnit(costPerUnit);
        materialRepo.save(material);

        return stockEntryDto(saved);
    }

    private Map<String, Object> stockEntryDto(StockEntry e) {
        Map<String, Object> dto = new java.util.LinkedHashMap<>();
        dto.put("id", e.getId());
        dto.put("materialId", e.getMaterial().getId());
        dto.put("materialName", e.getMaterial().getName());
        dto.put("date", e.getDate().toString());
        dto.put("quantity", e.getQuantity());
        dto.put("costPerUnit", e.getCostPerUnit());
        dto.put("transportCost", e.getTransportCost());
        dto.put("miscCost", e.getMiscCost());
        dto.put("totalCost", e.getTotalCost());
        dto.put("supplier", e.getSupplier());
        dto.put("note", e.getNote());
        return dto;
    }
}
