package com.mumsaqua.repository;

import com.mumsaqua.entity.FuelPrice;
import com.mumsaqua.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelPriceRepository extends JpaRepository<FuelPrice, Vehicle.FuelType> {
}
