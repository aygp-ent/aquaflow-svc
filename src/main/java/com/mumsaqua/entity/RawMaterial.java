package com.mumsaqua.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "raw_materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RawMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType type;

    private String bottleSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialUnit unit;

    @Column(nullable = false)
    private Integer currentStock = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    public enum MaterialType { BOTTLE, CAP, STICKER, PACKING, HANGER, CARTRIDGE }
    public enum MaterialUnit { PCS, KG }
}
