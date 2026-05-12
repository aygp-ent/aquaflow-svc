package com.mumsaqua.repository;

import com.mumsaqua.entity.FuelPriceHistory;
import com.mumsaqua.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FuelPriceHistoryRepository extends JpaRepository<FuelPriceHistory, Long> {
    List<FuelPriceHistory> findByFuelTypeOrderByEffectiveDateDesc(Vehicle.FuelType fuelType);
    List<FuelPriceHistory> findAllByOrderByEffectiveDateDesc();
}
