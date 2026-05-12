package com.mumsaqua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fuel_prices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FuelPrice {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Vehicle.FuelType fuelType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;
}
