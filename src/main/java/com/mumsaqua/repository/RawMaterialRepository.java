package com.mumsaqua.repository;

import com.mumsaqua.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    List<RawMaterial> findByCurrentStockLessThan(Integer threshold);
}
