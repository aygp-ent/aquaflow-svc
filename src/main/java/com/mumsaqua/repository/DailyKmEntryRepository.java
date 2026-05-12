package com.mumsaqua.repository;

import com.mumsaqua.entity.DailyKmEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyKmEntryRepository extends JpaRepository<DailyKmEntry, Long> {
    List<DailyKmEntry> findByDateOrderByIdDesc(LocalDate date);
    List<DailyKmEntry> findByDriverIdOrderByDateDesc(Long driverId);
    List<DailyKmEntry> findByDateAndDriverId(LocalDate date, Long driverId);
}
