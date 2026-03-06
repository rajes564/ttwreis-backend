package com.ttwreis.controller;

import com.ttwreis.dto.AdminStatsDto;
import com.ttwreis.dto.InvigilatorRequest;
import com.ttwreis.entity.ExamCenter;
import com.ttwreis.entity.Institute;
import com.ttwreis.entity.Invigilator;
import com.ttwreis.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ── Stats ──────────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ── Applications (pageable, filterable) ────────────────────────────────────
    @GetMapping("/applications")
    public ResponseEntity<Page<Map<String, Object>>> getApplications(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "")  String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getApplicationsPage(search, status, pageable));
    }

    // ── Candidates (pageable) ──────────────────────────────────────────────────
    @GetMapping("/candidates")
    public ResponseEntity<Page<Map<String, Object>>> getCandidates(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getCandidatesPage(search, pageable));
    }


    // ── Institutes list for dropdowns ─────────────────────────────────────────
    @GetMapping("/institutes/all")
    public ResponseEntity<?> getAllInstitutes() {
        return ResponseEntity.ok(adminService.getInstitutes());
    }

    // ── Institutes (pageable) ──────────────────────────────────────────────────
    @PostMapping("/institutes")
    public ResponseEntity<Institute> addInstitute(@RequestBody Institute institute) {
        return ResponseEntity.ok(adminService.saveInstitute(institute));
    }

    @GetMapping("/institutes")
    public ResponseEntity<Page<Map<String, Object>>> getInstitutes(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getInstitutesPage(search, PageRequest.of(page, size)));
    }

    // ── Exam Centers (pageable) ────────────────────────────────────────────────
    @PostMapping("/exam-centers")
    public ResponseEntity<ExamCenter> addExamCenter(@RequestBody ExamCenter examCenter) {
        return ResponseEntity.ok(adminService.saveExamCenter(examCenter));
    }

    @GetMapping("/exam-centers")
    public ResponseEntity<Page<Map<String, Object>>> getExamCenters(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getExamCentersPage(search, PageRequest.of(page, size)));
    }

    // ── Exam Centers list (for dropdown in invigilator form) ──────────────────
    @GetMapping("/exam-centers/all")
    public ResponseEntity<?> getAllExamCenters() {
        return ResponseEntity.ok(adminService.getExamCenters());
    }

    // ── Invigilators (pageable) ────────────────────────────────────────────────
    @PostMapping("/invigilators")
    public ResponseEntity<Invigilator> addInvigilator(@RequestBody InvigilatorRequest request) {
        return ResponseEntity.ok(adminService.saveInvigilator(request));
    }

    @GetMapping("/invigilators")
    public ResponseEntity<Page<Map<String, Object>>> getInvigilators(
            @RequestParam(defaultValue = "")  String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getInvigilatorsPage(search, PageRequest.of(page, size)));
    }
}
