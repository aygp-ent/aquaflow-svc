package com.mumsaqua.controller;

import com.mumsaqua.entity.*;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CostController {

    private final ProductRepository productRepo;
    private final RawMaterialRepository materialRepo;
    private final LabourRepository labourRepo;
    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;

    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int estimatedBottles = Integer.parseInt(body.get("estimatedBottles").toString());

        Product product = productRepo.findById(productId).orElseThrow();
        String bottleSize = product.getBottleSize() != null ? product.getBottleSize() : "1L";

        // Material cost per bottle (based on bottle size)
        Map<String, BigDecimal> materialCostMap = Map.of(
                "250ML", BigDecimal.valueOf(2.20),
                "500ML", BigDecimal.valueOf(3.10),
                "1L", BigDecimal.valueOf(4.30),
                "2L", BigDecimal.valueOf(6.50)
        );
        BigDecimal materialCostPerBottle = materialCostMap.getOrDefault(bottleSize, BigDecimal.valueOf(4.00));

        // If we have actual material costs, use average
        List<RawMaterial> materials = materialRepo.findAll();
        if (!materials.isEmpty()) {
            BigDecimal avgCost = materials.stream()
                    .map(RawMaterial::getCostPerUnit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(materials.size()), 2, RoundingMode.HALF_UP);
            if (avgCost.compareTo(BigDecimal.ZERO) > 0) {
                materialCostPerBottle = avgCost;
            }
        }

        // Labour cost per bottle
        BigDecimal totalLabourMonthlyCost = labourRepo.findAll().stream()
                .map(Labour::getMonthlySalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal labourCostPerBottle = estimatedBottles > 0
                ? totalLabourMonthlyCost.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(estimatedBottles), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Transport cost per bottle
        BigDecimal totalVehicleRent = vehicleRepo.findAll().stream()
                .map(Vehicle::getDailyRent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDriverSalary = driverRepo.findAll().stream()
                .map(d -> d.getSalaryBasis() == Driver.SalaryBasis.DAILY
                        ? d.getSalary() : d.getSalary().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dailyTransportCost = totalVehicleRent.add(totalDriverSalary);
        BigDecimal transportCostPerBottle = estimatedBottles > 0
                ? dailyTransportCost.divide(BigDecimal.valueOf(estimatedBottles), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalCostPerBottle = materialCostPerBottle.add(labourCostPerBottle).add(transportCostPerBottle);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("materialCostPerBottle", materialCostPerBottle.setScale(2, RoundingMode.HALF_UP));
        result.put("labourCostPerBottle", labourCostPerBottle.setScale(2, RoundingMode.HALF_UP));
        result.put("transportCostPerBottle", transportCostPerBottle.setScale(2, RoundingMode.HALF_UP));
        result.put("totalCostPerBottle", totalCostPerBottle.setScale(2, RoundingMode.HALF_UP));
        return result;
    }
}
