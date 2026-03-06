package com.ttwreis.controller;

import com.ttwreis.entity.User;
import com.ttwreis.repository.UserRepository;
import com.ttwreis.service.ApplicationService;
import com.ttwreis.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/applicant")
@RequiredArgsConstructor
public class ApplicantController {

    private final ApplicationService applicationService;
    private final PdfService         pdfService;
    private final UserRepository     userRepository;

    // ── Helper ────────────────────────────────────────────────────────────────

    private User currentUser() {
        String regNo = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new RuntimeException("User not found: " + regNo));
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * GET /api/applicant/application-status
     * Returns dashboard summary for the logged-in candidate.
     */
    @GetMapping("/application-status")
    public ResponseEntity<Map<String, Object>> getApplicationStatus() {
    	
        return ResponseEntity.ok(applicationService.getApplicationStatus());
    }

    /**
     * GET /api/applicant/prefilled-data
     * Returns registration data to pre-fill the application form.
     */
    @GetMapping("/prefilled-data")
    public ResponseEntity<Map<String, Object>> getPrefilledData() {
        return ResponseEntity.ok(applicationService.getPrefilledData());
    }

    /**
     * POST /api/applicant/submit-application
     * Body: flat JSON map with all form fields + photo/signature base64.
     */
    @PostMapping("/submit-application")
    public ResponseEntity<Map<String, Object>> submitApplication(
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(applicationService.submitApplication(payload));
    }

    /**
     * GET /api/applicant/download-application
     * Streams the application PDF for the current candidate.
     */
    @GetMapping("/download-application")
    public ResponseEntity<byte[]> downloadApplication() {
        try {
            User   user = currentUser();
            byte[] pdf  = pdfService.generateApplicationPdf(user.getId());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"Application_" + user.getRegistrationNumber() + ".pdf\"")
                    .body(pdf);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("PDF generation failed: " + ex.getMessage()).getBytes());
        }
    }

    /**
     * GET /api/applicant/hall-ticket
     * Returns 404 until hall tickets are generated (exam center allocated).
     */
    @GetMapping("/hall-ticket")
    public ResponseEntity<byte[]> downloadHallTicket() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Hall ticket not yet available. Please check back after exam center allocation.".getBytes());
    }
}
