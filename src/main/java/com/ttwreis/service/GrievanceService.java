package com.ttwreis.service;

import com.ttwreis.entity.Grievance;
import com.ttwreis.entity.Grievance.GrievanceStatus;
import com.ttwreis.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    // ── Public: Submit a new grievance ────────────────────────────────────────
    @Transactional
    public Map<String, Object> submit(Map<String, String> req) {
        String grNo = "GRV" + LocalDateTime.now().format(DT)
                    + String.format("%03d", (int)(Math.random() * 1000));

        Grievance g = Grievance.builder()
                .grievanceNumber   (grNo)
                .applicantName     (req.get("applicantName"))
                .mobileNumber      (req.get("mobileNumber"))
                .email             (req.getOrDefault("email", ""))
                .registrationNumber(req.getOrDefault("registrationNumber", ""))
                .category          (req.get("category"))
                .description       (req.get("description"))
                .status            (GrievanceStatus.OPEN)
                .build();

        grievanceRepository.save(g);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("grievanceNumber", grNo);
        res.put("message", "Grievance submitted successfully. Use your Grievance Number to track status.");
        return res;
    }

    // ── Public: Track by grievance number ────────────────────────────────────
    public Map<String, Object> track(String grievanceNumber) {
        Grievance g = grievanceRepository.findByGrievanceNumber(grievanceNumber)
                .orElseThrow(() -> new RuntimeException("Grievance not found: " + grievanceNumber));
        return toMap(g);
    }

    // ── Applicant: My grievances by registration number ───────────────────────
    public Page<Map<String, Object>> myGrievances(String regNo, Pageable pageable) {
        return grievanceRepository
                .findByRegistrationNumberOrderByCreatedAtDesc(regNo, pageable)
                .map(this::toMap);
    }

    // ── Admin: Pageable list ──────────────────────────────────────────────────
    public Page<Map<String, Object>> adminList(String search, String statusStr, Pageable pageable) {
        GrievanceStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = GrievanceStatus.valueOf(statusStr.trim().toUpperCase()); } catch (Exception ignored) {}
        }
        return grievanceRepository.searchGrievances(status, search, pageable).map(this::toMap);
    }

    // ── Admin: Update status + remarks ────────────────────────────────────────
    @Transactional
    public Map<String, Object> updateStatus(Long id, String statusStr, String remarks, String resolvedBy) {
        Grievance g = grievanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found: " + id));

        GrievanceStatus newStatus = GrievanceStatus.valueOf(statusStr.toUpperCase());
        g.setStatus(newStatus);
        g.setAdminRemarks(remarks);
        g.setResolvedBy(resolvedBy);
        g.setUpdatedAt(LocalDateTime.now());
        if (newStatus == GrievanceStatus.RESOLVED || newStatus == GrievanceStatus.CLOSED) {
            g.setResolvedAt(LocalDateTime.now());
        }
        grievanceRepository.save(g);
        return toMap(g);
    }

    // ── Stats for admin dashboard ─────────────────────────────────────────────
    public Map<String, Long> stats() {
        Map<String, Long> s = new LinkedHashMap<>();
        s.put("total",      grievanceRepository.count());
        s.put("open",       grievanceRepository.countByStatus(GrievanceStatus.OPEN));
        s.put("inProgress", grievanceRepository.countByStatus(GrievanceStatus.IN_PROGRESS));
        s.put("resolved",   grievanceRepository.countByStatus(GrievanceStatus.RESOLVED));
        s.put("closed",     grievanceRepository.countByStatus(GrievanceStatus.CLOSED));
        return s;
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Grievance g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                 g.getId());
        m.put("grievanceNumber",    g.getGrievanceNumber());
        m.put("applicantName",      g.getApplicantName());
        m.put("mobileNumber",       g.getMobileNumber());
        m.put("email",              g.getEmail());
        m.put("registrationNumber", g.getRegistrationNumber());
        m.put("category",           g.getCategory());
        m.put("description",        g.getDescription());
        m.put("status",             g.getStatus().name());
        m.put("adminRemarks",       g.getAdminRemarks());
        m.put("resolvedBy",         g.getResolvedBy());
        m.put("resolvedAt",         g.getResolvedAt() != null ? g.getResolvedAt().toLocalDate().toString() : null);
        m.put("createdAt",          g.getCreatedAt() != null ? g.getCreatedAt().toLocalDate().toString() : null);
        m.put("updatedAt",          g.getUpdatedAt() != null ? g.getUpdatedAt().toLocalDate().toString() : null);
        return m;
    }
}