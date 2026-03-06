package com.ttwreis.service;

import com.ttwreis.entity.*;
import com.ttwreis.entity.Application.ApplicationStatus;
import com.ttwreis.entity.Payment.PaymentStatus;
import com.ttwreis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository     applicationRepository;
    private final UserRepository            userRepository;
    private final PaymentRepository         paymentRepository;
    private final CollegePriorityRepository collegePriorityRepository;
    private final InstituteRepository       instituteRepository;

    private User currentUser() {
        String regNo = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + regNo));
    }

    private String generateApplicationNumber() {
        return "APP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString() : null;
    }

    private Boolean parseBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        String s = v.toString().trim();
        return "YES".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
    }

    // ── Application status (for dashboard) ────────────────────────────────────
    @Transactional
    public Map<String, Object> getApplicationStatus() {
        User user = currentUser();
        Optional<Application> opt = applicationRepository.findByUserId(user.getId());

        Map<String, Object> res = new LinkedHashMap<>();
        if (opt.isEmpty()) {
            res.put("status",              "NOT_STARTED");
            res.put("applicationFilled",   false);
            res.put("paymentStatus",       "PENDING");
            res.put("hallTicketAvailable", false);
            res.put("appeared",            false);
            res.put("seatAllotted",        false);
            res.put("collegePriorities",   List.of());
        } else {
            Application app = opt.get();
            // "filled" = any status beyond DRAFT
            boolean filled = app.getStatus() != ApplicationStatus.DRAFT;

            // Fetch linked payment from payment table
            Optional<Payment> payOpt = paymentRepository.findByApplicationId(app.getId());
            PaymentStatus ps = payOpt.map(Payment::getStatus).orElse(PaymentStatus.PENDING);
            Double amountPaid = payOpt
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .orElse(null);

            res.put("applicationNumber",   app.getApplicationNumber());
            res.put("status",              app.getStatus().name());
            res.put("applicationFilled",   filled);
            res.put("paymentStatus",       ps.name());
            res.put("amountPaid",          amountPaid);
            res.put("hallTicketAvailable", app.getHallTicketNumber() != null);
            res.put("appeared",            Boolean.TRUE.equals(app.getAppeared()));
            res.put("seatAllotted",        app.getStatus() == ApplicationStatus.ALLOTTED);
            res.put("submittedAt",
                    app.getSubmittedAt() != null
                    ? app.getSubmittedAt().toLocalDate().toString() : null);

            if (app.getStatus() == ApplicationStatus.ALLOTTED) {
                res.put("allottedCollege", app.getAllottedCollege());
                res.put("allottedStream",  app.getAllottedStream());
                res.put("allotmentPhase",  app.getAllotmentPhase());
                res.put("joiningDate",     app.getJoiningDate());
            }

            List<Map<String, Object>> prioList = new ArrayList<>();
            for (CollegePriority cp : app.getCollegePriorities()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("order", cp.getPriorityOrder());
                m.put("name",  cp.getCollegeName());
                m.put("instituteId", cp.getInstitute() != null ? cp.getInstitute().getId() : null);
                prioList.add(m);
            }
            res.put("collegePriorities", prioList);
        }
        return res;
    }

    // ── Prefilled data (Step 0 of form) ───────────────────────────────────────
    @Transactional
    public Map<String, Object> getPrefilledData() {
        User user = currentUser();
        Map<String, Object> data = new LinkedHashMap<>();

        // Personal data from User table
        data.put("candidateName",   user.getCandidateName());
        data.put("fatherName",      user.getFatherName());
        data.put("motherName",      user.getMotherName());
        data.put("dateOfBirth",
                user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        data.put("email",           user.getEmail());
        data.put("mobileNumber",    user.getMobileNumber());
        data.put("alternateMobile", user.getAlternateMobile());
        data.put("gender",          user.getGender() != null ? user.getGender().name() : null);
        data.put("idType",          user.getIdType());
        data.put("idNumber",        user.getIdNumber());
        data.put("aadhaarNumber",   user.getAadhaarNumber());

        // Addresses from registration — frontend should NOT ask again
        data.put("pCountry",    user.getPCountry());
        data.put("pState",      user.getPState());
        data.put("pDistrict",   user.getPDistrict());
        data.put("pMandal",     user.getPMandal());
        data.put("pVillage",    user.getPVillage());
        data.put("pPincode",    user.getPPincode());
        data.put("permCountry", user.getPermCountry());
        data.put("permState",   user.getPermState());
        data.put("permDistrict",user.getPermDistrict());
        data.put("permMandal",  user.getPermMandal());
        data.put("permVillage", user.getPermVillage());
        data.put("permPincode", user.getPermPincode());

        // Restore any previously-saved application data
        applicationRepository.findByUserId(user.getId()).ifPresent(app -> {
            data.put("applicationNumber",    app.getApplicationNumber());
            data.put("classApplied",         app.getClassApplied());
            data.put("stream",               app.getStream());
            data.put("educationalStatus",    app.getEducationalStatus());
            data.put("community",            app.getCommunity());
            data.put("subCaste",             app.getSubCaste());
            data.put("incomeBelowThreshold",
                    app.getIncomeBelowThreshold() != null
                    ? (app.getIncomeBelowThreshold() ? "YES" : "NO") : null);
            data.put("photoBase64",          app.getPhotoBase64());
            data.put("signatureBase64",      app.getSignatureBase64());
            data.put("status",               app.getStatus().name());

            // Payment status from linked payment record
            paymentRepository.findByApplicationId(app.getId()).ifPresent(pay -> {
                data.put("paymentStatus", pay.getStatus().name());
                if (pay.getStatus() == PaymentStatus.PAID) {
                    data.put("amountPaid", pay.getAmount());
                    data.put("paidAt",     pay.getPaidAt() != null ? pay.getPaidAt().toString() : null);
                }
            });

            List<Map<String, Object>> prioList = new ArrayList<>();
            for (CollegePriority cp : app.getCollegePriorities()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("order",       cp.getPriorityOrder());
                m.put("name",        cp.getCollegeName());
                m.put("instituteId", cp.getInstitute() != null ? cp.getInstitute().getId() : null);
                prioList.add(m);
            }
            data.put("collegePriorities", prioList);
        });
        return data;
    }

    // ── Submit application ─────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> submitApplication(Map<String, Object> payload) {
        User user = currentUser();

        Application app = applicationRepository.findByUserId(user.getId())
                .orElse(Application.builder()
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .build());

        if (app.getApplicationNumber() == null) {
            app.setApplicationNumber(generateApplicationNumber());
        }

        // Academic details
        app.setClassApplied     (str(payload, "classApplied"));
        app.setStream           (str(payload, "stream"));
        app.setEducationalStatus(str(payload, "educationalStatus"));

        // Caste / community
        app.setCommunity           (str(payload, "community"));
        app.setSubCaste            (str(payload, "subCaste"));
        app.setIncomeBelowThreshold(parseBool(payload.get("incomeBelowThreshold")));

        // Photo / signature
        app.setPhotoBase64    (str(payload, "photo"));
        app.setSignatureBase64(str(payload, "signature"));

        // Status: SUBMITTED (payment pending)
        // Only update to SUBMITTED if currently DRAFT — keep PAID status intact
        if (app.getStatus() == null || app.getStatus() == ApplicationStatus.DRAFT) {
            app.setStatus(ApplicationStatus.SUBMITTED);
        }
        app.setUpdatedAt(LocalDateTime.now());
        if (app.getSubmittedAt() == null) {
            app.setSubmittedAt(LocalDateTime.now());
        }

        Application saved = applicationRepository.save(app);

        // ── College Priorities (replace all) ──────────────────────────────────
        collegePriorityRepository.deleteByApplicationId(saved.getId());
        collegePriorityRepository.flush(); // ensure deletes committed before re-insert

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> priorities =
            (List<Map<String, Object>>) payload.get("collegePriorities");

        if (priorities != null) {
            int order = 1;
            for (Map<String, Object> p : priorities) {
                String name = p.get("name") != null ? p.get("name").toString() : null;
                if (name == null || name.isBlank()) continue;

                Institute institute = null;
                Object instId = p.get("instituteId");
                if (instId != null) {
                    try {
                        institute = instituteRepository
                                .findById(Long.parseLong(instId.toString())).orElse(null);
                    } catch (Exception ignored) {}
                }

                collegePriorityRepository.save(CollegePriority.builder()
                        .application(saved)
                        .priorityOrder(order++)
                        .collegeName(name)
                        .institute(institute)
                        .build());
            }
        }

        // ── Ensure Payment record exists (PENDING) — never overwrite PAID ─────
        Optional<Payment> existingPay = paymentRepository.findByApplicationId(saved.getId());
        if (existingPay.isEmpty()) {
            paymentRepository.save(Payment.builder()
                    .application(saved)
                    .status(PaymentStatus.PENDING)
                    .amount(100.0)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return Map.of(
            "applicationNumber", saved.getApplicationNumber(),
            "message",           "Application submitted successfully! Please pay ₹100 to confirm.",
            "paymentRequired",   true
        );
    }
}
