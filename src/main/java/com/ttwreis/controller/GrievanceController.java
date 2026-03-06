package com.ttwreis.controller;

import com.ttwreis.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;

    /** Public — anyone can submit */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(grievanceService.submit(req));
    }

    /** Public — track by grievance number */
    @GetMapping("/track/{grievanceNumber}")
    public ResponseEntity<Map<String, Object>> track(@PathVariable String grievanceNumber) {
        return ResponseEntity.ok(grievanceService.track(grievanceNumber));
    }

    /** Applicant — my grievances (requires JWT) */
    @GetMapping("/my")
    public ResponseEntity<Page<Map<String, Object>>> myGrievances(
            @RequestParam String regNo,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            grievanceService.myGrievances(regNo, PageRequest.of(page, size)));
    }

    /** Admin — list all */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Map<String, Object>>> adminList(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(
            grievanceService.adminList(search, status, PageRequest.of(page, size)));
    }

    /** Admin — update status & remarks */
    @PutMapping("/admin/{id}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(grievanceService.updateStatus(
                id,
                body.get("status"),
                body.get("remarks"),
                body.getOrDefault("resolvedBy", "Admin")));
    }

    /** Admin — stats */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(grievanceService.stats());
    }
}