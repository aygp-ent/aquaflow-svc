package com.mumsaqua.config;

import com.mumsaqua.entity.*;
import com.mumsaqua.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepo;
    private final FuelPriceRepository fuelPriceRepo;
    private final ProductRepository productRepo;
    private final RawMaterialRepository rawMaterialRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default users if none exist
        if (userRepo.count() == 0) {
            userRepo.save(AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .role(AppUser.UserRole.ADMIN)
                    .displayName("Admin")
                    .active(true)
                    .build());

            userRepo.save(AppUser.builder()
                    .username("salesman")
                    .password(passwordEncoder.encode("password"))
                    .role(AppUser.UserRole.SALESMAN)
                    .displayName("Salesman")
                    .active(true)
                    .build());

            userRepo.save(AppUser.builder()
                    .username("driver")
                    .password(passwordEncoder.encode("password"))
                    .role(AppUser.UserRole.DRIVER)
                    .displayName("Driver")
                    .active(true)
                    .build());

            log.info("✓ Default users created: admin, salesman, driver (password: password)");
        }

        // Seed fuel prices if none exist
        if (fuelPriceRepo.count() == 0) {
            fuelPriceRepo.save(FuelPrice.builder().fuelType(Vehicle.FuelType.DIESEL).pricePerUnit(BigDecimal.valueOf(89.50)).build());
            fuelPriceRepo.save(FuelPrice.builder().fuelType(Vehicle.FuelType.PETROL).pricePerUnit(BigDecimal.valueOf(104.20)).build());
            fuelPriceRepo.save(FuelPrice.builder().fuelType(Vehicle.FuelType.CNG).pricePerUnit(BigDecimal.valueOf(76.00)).build());
            fuelPriceRepo.save(FuelPrice.builder().fuelType(Vehicle.FuelType.EV).pricePerUnit(BigDecimal.valueOf(8.50)).build());
            log.info("✓ Default fuel prices seeded");
        }

        // Seed products if none exist
        if (productRepo.count() == 0) {
            productRepo.save(Product.builder().name("Water 2L").bottleSize("2L").bottlesPerPatti(6).status(Product.Status.ACTIVE).build());
            productRepo.save(Product.builder().name("Water 1L").bottleSize("1L").bottlesPerPatti(12).status(Product.Status.ACTIVE).build());
            productRepo.save(Product.builder().name("Water 500ML").bottleSize("500ML").bottlesPerPatti(24).status(Product.Status.ACTIVE).build());
            productRepo.save(Product.builder().name("Water 250ML").bottleSize("250ML").bottlesPerPatti(48).status(Product.Status.ACTIVE).build());
            log.info("✓ Products seeded: 2L, 1L, 500ML, 250ML");
        }

        // Seed raw materials if none exist (stock = 0)
        if (rawMaterialRepo.count() == 0) {
            // Bottles
            rawMaterialRepo.save(RawMaterial.builder().name("Bottle 2L").type(RawMaterial.MaterialType.BOTTLE).bottleSize("2L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Bottle 1L").type(RawMaterial.MaterialType.BOTTLE).bottleSize("1L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Bottle 500ML").type(RawMaterial.MaterialType.BOTTLE).bottleSize("500ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Bottle 250ML").type(RawMaterial.MaterialType.BOTTLE).bottleSize("250ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());

            // Caps
            rawMaterialRepo.save(RawMaterial.builder().name("Cap 2L").type(RawMaterial.MaterialType.CAP).bottleSize("2L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Cap 1L").type(RawMaterial.MaterialType.CAP).bottleSize("1L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Cap 500ML").type(RawMaterial.MaterialType.CAP).bottleSize("500ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Cap 250ML").type(RawMaterial.MaterialType.CAP).bottleSize("250ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());

            // Stickers
            rawMaterialRepo.save(RawMaterial.builder().name("Sticker 2L").type(RawMaterial.MaterialType.STICKER).bottleSize("2L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Sticker 1L").type(RawMaterial.MaterialType.STICKER).bottleSize("1L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Sticker 500ML").type(RawMaterial.MaterialType.STICKER).bottleSize("500ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Sticker 250ML").type(RawMaterial.MaterialType.STICKER).bottleSize("250ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());

            // Packing
            rawMaterialRepo.save(RawMaterial.builder().name("Packing 2L").type(RawMaterial.MaterialType.PACKING).bottleSize("2L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Packing 1L").type(RawMaterial.MaterialType.PACKING).bottleSize("1L").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Packing 500ML").type(RawMaterial.MaterialType.PACKING).bottleSize("500ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());
            rawMaterialRepo.save(RawMaterial.builder().name("Packing 250ML").type(RawMaterial.MaterialType.PACKING).bottleSize("250ML").unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());

            // Cartridge (shared across sizes)
            rawMaterialRepo.save(RawMaterial.builder().name("Cartridge Filter").type(RawMaterial.MaterialType.CARTRIDGE).unit(RawMaterial.MaterialUnit.PCS).currentStock(0).costPerUnit(BigDecimal.ZERO).build());

            log.info("✓ Raw materials seeded: Bottles, Caps, Stickers, Packing (2L, 1L, 500ML, 250ML) + Cartridge");
        }
    }
}
