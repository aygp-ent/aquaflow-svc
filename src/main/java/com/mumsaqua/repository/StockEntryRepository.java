package com.mumsaqua.repository;

import com.mumsaqua.entity.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockEntryRepository extends JpaRepository<StockEntry, Long> {
    List<StockEntry> findByMaterialIdOrderByDateDesc(Long materialId);
}
