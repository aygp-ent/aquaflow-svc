package com.mumsaqua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_km_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyKmEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer kmStart;

    @Column(nullable = false)
    private Integer kmEnd;

    @Column(nullable = false)
    private Integer personalKm = 0;

    @Column(nullable = false)
    private Integer totalKm;

    @Column(nullable = false)
    private Integer companyKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fuelCost; // auto-calculated

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fuelExpense; // actual amount driver spent

    private String note;
}
