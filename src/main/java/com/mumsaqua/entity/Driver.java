package com.mumsaqua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "drivers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryBasis salaryBasis = SalaryBasis.DAILY;

    @OneToOne(mappedBy = "assignedDriver")
    private Vehicle assignedVehicle;

    public enum SalaryBasis { DAILY, MONTHLY }
}
