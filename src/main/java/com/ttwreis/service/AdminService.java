package com.ttwreis.service;

import com.ttwreis.dto.AdminStatsDto;
import com.ttwreis.dto.InvigilatorRequest;
import com.ttwreis.entity.*;
import com.ttwreis.entity.Application.ApplicationStatus;
import com.ttwreis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ApplicationRepository  applicationRepository;
    private final InstituteRepository    instituteRepository;
    private final ExamCenterRepository   examCenterRepository;
    private final InvigilatorRepository  invigilatorRepository;
    private final UserRepository         userRepository;

    // ── Stats ─────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        long total  = applicationRepository.count();
        long paid   = applicationRepository.countPaid();
        double amount = applicationRepository.findAll().stream()
                .filter(a -> a.getPayment() != null
                          && a.getPayment().getStatus() == Payment.PaymentStatus.PAID
                          && a.getPayment().getAmount() != null)
                .mapToDouble(a -> a.getPayment().getAmount()).sum();

        List<Object[]> rawCat = applicationRepository.countGroupedByCommunity();
        List<Map<String, Object>> catBreakdown = new ArrayList<>();
        for (Object[] row : rawCat) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", row[0]); m.put("value", row[1]);
            catBreakdown.add(m);
        }
        if (catBreakdown.isEmpty()) catBreakdown = defaultCategories();

        List<Institute> institutes = instituteRepository.findByActiveTrue();
        List<Map<String, Object>> collegeWise = new ArrayList<>();
        for (Institute inst : institutes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name",   inst.getName());
            m.put("filled", inst.getFilledSeats() != null ? inst.getFilledSeats() : 0);
            m.put("vacant", inst.getVacantSeats());
            collegeWise.add(m);
        }
        if (collegeWise.isEmpty()) collegeWise = defaultColleges();

        long candidates = userRepository.findByRole(User.Role.CANDIDATE).size();

        return AdminStatsDto.builder()
                .totalApplications   (total)
                .paidApplications    (paid)
                .pendingApplications (total - paid)
                .totalAmountReceived (amount)
                .hallTicketsGenerated(0) //later integrate
                .appeared            (0)
                .notAppeared         (0)
                .categoryBreakdown   (catBreakdown)
                .collegeWise         (collegeWise)
                .build();
    }

    // ── Applications page ─────────────────────────────────────────────────────
    // @Transactional keeps the Hibernate session open while .map() accesses lazy associations
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getApplicationsPage(String search, String statusStr, Pageable pageable) {
        ApplicationStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = ApplicationStatus.valueOf(statusStr.trim().toUpperCase()); } catch (Exception ignored) {}
        }
        Page<Application> page = applicationRepository.searchApplications(status, search, pageable);
        return page.map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",                a.getId());
            m.put("applicationNumber", a.getApplicationNumber());

            // Safe: @Transactional keeps session alive for lazy-loading
            User u = a.getUser();
            m.put("candidateName",     u != null ? u.getCandidateName()      : "");
            m.put("registrationNumber",u != null ? u.getRegistrationNumber() : "");
            m.put("mobileNumber",      u != null ? u.getMobileNumber()        : "");
            m.put("gender",            u != null && u.getGender() != null
                                            ? u.getGender().name() : "");

            m.put("community",    a.getCommunity());
            m.put("classApplied", a.getClassApplied());
            m.put("stream",       a.getStream());
            m.put("status",       a.getStatus() != null ? a.getStatus().name() : "DRAFT");

            // Payment fields — lazy @OneToOne, safe inside transaction
            Payment pay = a.getPayment();
            m.put("paymentStatus", pay != null ? pay.getStatus().name() : "PENDING");
            m.put("amountPaid",    pay != null && pay.getStatus() == Payment.PaymentStatus.PAID
                                        ? pay.getAmount() : null);
            m.put("transactionId", pay != null ? pay.getTransactionId() : null);
            m.put("paidAt",        pay != null && pay.getPaidAt() != null
                                        ? pay.getPaidAt().toLocalDate().toString() : "");

            m.put("submittedAt",   a.getSubmittedAt() != null
                                        ? a.getSubmittedAt().toLocalDate().toString() : "");
            return m;
        });
    }

    // ── Candidates page ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getCandidatesPage(String search, Pageable pageable) {
        Page<User> page = userRepository.searchCandidates(search, pageable);
        return page.map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("registrationNumber", u.getRegistrationNumber());
            m.put("candidateName",      u.getCandidateName());
            m.put("gender",             u.getGender() != null ? u.getGender().name() : "");
            m.put("mobileNumber",       u.getMobileNumber());
            m.put("email",              u.getEmail());
            m.put("aadhaarNumber",      mask(u.getAadhaarNumber()));
            m.put("dateOfBirth",        u.getDateOfBirth() != null ? u.getDateOfBirth().toString() : "");
            m.put("pState",             u.getPState());
            m.put("pDistrict",          u.getPDistrict());
            m.put("active",             u.isActive());
            return m;
        });
    }

    // ── Institutes page ───────────────────────────────────────────────────────
    public Page<Map<String, Object>> getInstitutesPage(String search, Pageable pageable) {
        return instituteRepository.searchInstitutes(search, pageable).map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          i.getId());
            m.put("name",        i.getName());
            m.put("code",        i.getCode());
            m.put("district",    i.getDistrict());
            m.put("mandal",      i.getMandal());
            m.put("address",     i.getAddress());
            m.put("type",        i.getType());
            m.put("totalSeats",  i.getTotalSeats());
            m.put("filledSeats", i.getFilledSeats() != null ? i.getFilledSeats() : 0);
            m.put("vacantSeats", i.getVacantSeats());
            m.put("active",      i.isActive());
            return m;
        });
    }

    // ── Exam Centers page ─────────────────────────────────────────────────────
    public Page<Map<String, Object>> getExamCentersPage(String search, Pageable pageable) {
        return examCenterRepository.searchCenters(search, pageable).map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           e.getId());
            m.put("name",         e.getName());
            m.put("district",     e.getDistrict());
            m.put("address",      e.getAddress());
            m.put("noOfRooms",    e.getNoOfRooms());
            m.put("roomCapacity", e.getRoomCapacity());
            m.put("totalCapacity",(e.getNoOfRooms() != null && e.getRoomCapacity() != null)
                                    ? e.getNoOfRooms() * e.getRoomCapacity() : 0);
            m.put("active",       e.isActive());
            return m;
        });
    }

    // ── Invigilators page ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getInvigilatorsPage(String search, Pageable pageable) {
        return invigilatorRepository.searchInvigilators(search, pageable).map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          i.getId());
            m.put("name",        i.getName());
            m.put("designation", i.getDesignation());
            m.put("mobile",      i.getMobile());
            // examCenter is also lazy — safe here because @Transactional
            m.put("examCenter",  i.getExamCenter() != null ? i.getExamCenter().getName() : "—");
            m.put("active",      i.isActive());
            return m;
        });
    }

    // ── Save methods ──────────────────────────────────────────────────────────
    @Transactional
    public Institute saveInstitute(Institute institute) {
        if (institute.isActive() ) institute.setActive(true);
        if (institute.getFilledSeats() == null) institute.setFilledSeats(0);
        return instituteRepository.save(institute);
    }

    public List<Institute> getInstitutes() { return instituteRepository.findAll(); }

    @Transactional
    public ExamCenter saveExamCenter(ExamCenter examCenter) {
        if (examCenter.isActive()) examCenter.setActive(true);
        return examCenterRepository.save(examCenter);
    }

    public List<ExamCenter> getExamCenters() { return examCenterRepository.findAll(); }

    @Transactional
    public Invigilator saveInvigilator(InvigilatorRequest req) {
        ExamCenter center = req.getExamCenterId() != null
                ? examCenterRepository.findById(req.getExamCenterId()).orElse(null) : null;
        return invigilatorRepository.save(Invigilator.builder()
                .name(req.getName()).designation(req.getDesignation())
                .mobile(req.getMobile()).examCenter(center).active(true).build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String mask(String val) {
        if (val == null || val.length() < 4) return "****";
        return "****" + val.substring(val.length() - 4);
    }

    private List<Map<String, Object>> defaultCategories() {
        Object[][] d = {{"ST",2150L},{"SC",890L},{"BC-A",420L},{"BC-B",380L},{"OC",375L}};
        List<Map<String, Object>> l = new ArrayList<>();
        for (Object[] row : d) { Map<String,Object> m=new LinkedHashMap<>(); m.put("name",row[0]); m.put("value",row[1]); l.add(m); }
        return l;
    }

    private List<Map<String, Object>> defaultColleges() {
        Object[][] d = {{"KGBV Khammam",80L,20L},{"KGBV Warangal",72L,28L},{"KGBV Adilabad",65L,35L},{"KGBV Nalgonda",58L,42L},{"KGBV Karimnagar",70L,30L}};
        List<Map<String, Object>> l = new ArrayList<>();
        for (Object[] row : d) { Map<String,Object> m=new LinkedHashMap<>(); m.put("name",row[0]); m.put("filled",row[1]); m.put("vacant",row[2]); l.add(m); }
        return l;
    }
}