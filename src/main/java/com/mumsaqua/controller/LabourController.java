package com.mumsaqua.controller;

import com.mumsaqua.entity.Attendance;
import com.mumsaqua.entity.Labour;
import com.mumsaqua.repository.AttendanceRepository;
import com.mumsaqua.repository.LabourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LabourController {

    private final LabourRepository labourRepo;
    private final AttendanceRepository attendanceRepo;

    @GetMapping("/labour")
    public List<Labour> getAll() {
        return labourRepo.findAll();
    }

    @PostMapping("/labour")
    public Labour create(@RequestBody Labour labour) {
        return labourRepo.save(labour);
    }

    @PutMapping("/labour/{id}")
    public ResponseEntity<Labour> update(@PathVariable Long id, @RequestBody Labour data) {
        return labourRepo.findById(id).map(l -> {
            if (data.getName() != null) l.setName(data.getName());
            if (data.getMonthlySalary() != null) l.setMonthlySalary(data.getMonthlySalary());
            return ResponseEntity.ok(labourRepo.save(l));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/labour/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        labourRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Attendance ────────────────────────────────────────────────────────────

    @GetMapping("/attendance")
    public List<Map<String, Object>> getAttendance(@RequestParam(required = false) String date) {
        List<Attendance> list = date != null
                ? attendanceRepo.findByDate(LocalDate.parse(date))
                : attendanceRepo.findAll();

        return list.stream().map(a -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", a.getId());
            dto.put("labourId", a.getLabour().getId());
            dto.put("labourName", a.getLabour().getName());
            dto.put("date", a.getDate().toString());
            dto.put("present", a.getPresent());
            return dto;
        }).toList();
    }

    @PostMapping("/attendance")
    public Map<String, Object> markAttendance(@RequestBody Map<String, Object> body) {
        Long labourId = Long.valueOf(body.get("labourId").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());
        boolean present = Boolean.parseBoolean(body.get("present").toString());

        Labour labour = labourRepo.findById(labourId).orElseThrow();

        Attendance att = attendanceRepo.findByLabourIdAndDate(labourId, date)
                .orElse(Attendance.builder().labour(labour).date(date).build());
        att.setPresent(present);
        Attendance saved = attendanceRepo.save(att);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", saved.getId());
        dto.put("labourId", saved.getLabour().getId());
        dto.put("labourName", saved.getLabour().getName());
        dto.put("date", saved.getDate().toString());
        dto.put("present", saved.getPresent());
        return dto;
    }
}
