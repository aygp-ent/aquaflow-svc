package com.mumsaqua.repository;

import com.mumsaqua.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCustomerIdOrderBySaleDateDesc(Long customerId);
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDate from, LocalDate to);
    List<Sale> findAllByOrderBySaleDateDesc();
}
