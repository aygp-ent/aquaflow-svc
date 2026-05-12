package com.mumsaqua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType = FuelType.DIESEL;

    @Column(nullable = false)
    private Integer mileage = 10; // km per litre

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRent = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    public enum FuelType { DIESEL, PETROL, CNG, EV }
}
