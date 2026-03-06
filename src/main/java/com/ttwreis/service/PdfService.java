package com.ttwreis.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.ttwreis.entity.Application;
import com.ttwreis.entity.CollegePriority;
import com.ttwreis.entity.Payment;
import com.ttwreis.entity.User;
import com.ttwreis.repository.ApplicationRepository;
import com.ttwreis.repository.CollegePriorityRepository;
import com.ttwreis.repository.PaymentRepository;
import com.ttwreis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final ApplicationRepository     applicationRepository;
    private final UserRepository            userRepository;
    private final CollegePriorityRepository collegePriorityRepository;
    private final PaymentRepository         paymentRepository;

    // ── Brand colours ─────────────────────────────────────────────────────────
    private static final Color GREEN_DARK = new DeviceRgb(13,  59,  13);
    private static final Color GREEN_MID  = new DeviceRgb(27,  94,  32);
    private static final Color GREEN_BG   = new DeviceRgb(232, 245, 233);
    private static final Color ORANGE     = new DeviceRgb(230, 81,  0);
    private static final Color GOLD       = new DeviceRgb(255, 214, 0);
    private static final Color ROW_BORDER = new DeviceRgb(200, 230, 201);
    private static final Color PAID_GREEN = new DeviceRgb(56, 142, 60);
    private static final Color PENDING_ORANGE = new DeviceRgb(230, 81, 0);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter D_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── Static logo paths (place files in src/main/resources/static/) ─────────
    private static final String TTWREIS_LOGO   = "static/ttwreis-logo.jpg";
    private static final String TELANGANA_LOGO = "static/telangana-logo.png";

    @Transactional(readOnly = true)
    public byte[] generateApplicationPdf(Long userId) throws IOException {

        User        user      = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Application app       = applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Application not found for user: " + userId));
        List<CollegePriority> priorities =
                collegePriorityRepository.findByApplicationIdOrderByPriorityOrder(app.getId());
        Optional<Payment> payOpt = paymentRepository.findByApplicationId(app.getId());

        ByteArrayOutputStream baos   = new ByteArrayOutputStream();
        PdfWriter             writer = new PdfWriter(baos);
        PdfDocument           pdfDoc = new PdfDocument(writer);
        Document              doc    = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(20, 25, 20, 25);

        PdfFont bold    = PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ── Header: [TTWREIS logo] | [Centre text] | [Telangana logo] ─────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{12, 76, 12}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

        // Left – TTWREIS logo
        Cell leftLogoCell = noBorderCell()
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.LEFT);
        byte[] ttwreisLogo = loadStaticImage(TTWREIS_LOGO);
        if (ttwreisLogo != null) {
            leftLogoCell.add(new Image(ImageDataFactory.create(ttwreisLogo))
                    .setWidth(50).setHeight(55).setAutoScale(false));
        } else {
            leftLogoCell.add(para("TTWREIS", bold, 8, GREEN_DARK));
        }
        header.addCell(leftLogoCell);

        // Centre – organisation names
        Cell centreCell = noBorderCell().setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        centreCell.add(para("Telangana Tribal Welfare Residential Educational", bold, 14, GREEN_DARK));
        centreCell.add(para("Institutions Society (TGTWREIS)", bold, 14, GREEN_DARK).setMarginBottom(2));
        centreCell.add(para("Government of Telangana – Tribal Welfare Department", regular, 8, ORANGE));
        centreCell.add(para("TGTWREIS Admission 2026-27", bold, 13, GREEN_DARK).setMarginTop(3));
        header.addCell(centreCell);

        // Right – Telangana logo
        Cell rightLogoCell = noBorderCell()
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT);
        byte[] telanganaLogo = loadStaticImage(TELANGANA_LOGO);
        if (telanganaLogo != null) {
            rightLogoCell.add(new Image(ImageDataFactory.create(telanganaLogo))
                    .setWidth(50).setHeight(55).setAutoScale(false));
        } else {
            rightLogoCell.add(para("TELANGANA", bold, 8, GREEN_DARK));
        }
        header.addCell(rightLogoCell);

        doc.add(header);

        // Gold separator
        doc.add(new Paragraph()
                .setBorderBottom(new SolidBorder(GOLD, 3))
                .setMarginTop(0).setMarginBottom(6));

        // ── App number / Reg number row ───────────────────────────────────────
        Table appNoRow = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(4);
        appNoRow.addCell(noBorderCell()
                .add(para("Application No: " + nvl(app.getApplicationNumber()), bold, 10, GREEN_DARK)));
        appNoRow.addCell(noBorderCell().setTextAlignment(TextAlignment.RIGHT)
                .add(para("Reg No: " + user.getRegistrationNumber(), bold, 10, ORANGE)));
        doc.add(appNoRow);

        // ── Candidate photo ───────────────────────────────────────────────────
        if (hasContent(app.getPhotoBase64())) {
            try {
                byte[] imgBytes = decodeBase64Image(app.getPhotoBase64());
                Image  photo    = new Image(ImageDataFactory.create(imgBytes))
                        .setWidth(70).setHeight(90);
                Table photoRow = new Table(UnitValue.createPercentArray(new float[]{1, 70}))
                        .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(6);
                photoRow.addCell(noBorderCell());
                photoRow.addCell(noBorderCell().setTextAlignment(TextAlignment.RIGHT).add(photo));
                doc.add(photoRow);
            } catch (Exception ignored) {}
        }

        // ── Section 1 – Basic Details ─────────────────────────────────────────
        doc.add(sectionHeader("1. Personal Details", bold));
        Table basic = dataTable();
        addRow(basic, bold, regular, "Candidate Name",           nvl(user.getCandidateName()));
        addRow(basic, bold, regular, "Aadhaar Number",           nvl(user.getAadhaarNumber()));
        addRow(basic, bold, regular, "Father's / Guardian Name", nvl(user.getFatherName()));
        addRow(basic, bold, regular, "Mother's Name",            nvl(user.getMotherName()));
        addRow(basic, bold, regular, "Date of Birth",
                user.getDateOfBirth() != null ? user.getDateOfBirth().format(D_FMT) : "");
        addRow(basic, bold, regular, "Gender",
                user.getGender() != null ? user.getGender().name() : "");
        addRow(basic, bold, regular, "Email Address",   nvl(user.getEmail()));
        addRow(basic, bold, regular, "Mobile Number",   nvl(user.getMobileNumber()));
        addRow(basic, bold, regular, "ID Type",         nvl(user.getIdType()));
        addRow(basic, bold, regular, "ID Number",       nvl(user.getIdNumber()));
        doc.add(basic);

        // ── Section 2 – Present Address ───────────────────────────────────────
        doc.add(sectionHeader("2. Present Address", bold));
        Table pAddr = dataTable();
        addRow(pAddr, bold, regular, "Country",  nvl(user.getPCountry()));
        addRow(pAddr, bold, regular, "State",    nvl(user.getPState()));
        addRow(pAddr, bold, regular, "District", nvl(user.getPDistrict()));
        addRow(pAddr, bold, regular, "Mandal",   nvl(user.getPMandal()));
        addRow(pAddr, bold, regular, "Village",  nvl(user.getPVillage()));
        addRow(pAddr, bold, regular, "PIN Code", nvl(user.getPPincode()));
        doc.add(pAddr);

        // ── Section 3 – Permanent Address ────────────────────────────────────
        doc.add(sectionHeader("3. Permanent Address", bold));
        Table permAddr = dataTable();
        addRow(permAddr, bold, regular, "Country",  nvl(user.getPermCountry()));
        addRow(permAddr, bold, regular, "State",    nvl(user.getPermState()));
        addRow(permAddr, bold, regular, "District", nvl(user.getPermDistrict()));
        addRow(permAddr, bold, regular, "Mandal",   nvl(user.getPermMandal()));
        addRow(permAddr, bold, regular, "Village",  nvl(user.getPermVillage()));
        addRow(permAddr, bold, regular, "PIN Code", nvl(user.getPermPincode()));
        doc.add(permAddr);

        // ── Section 4 – Applied For ───────────────────────────────────────────
        doc.add(sectionHeader("4. Applied For", bold));
        Table applied = dataTable();
        addRow(applied, bold, regular, "Entrance into Class",  nvl(app.getClassApplied()));
        addRow(applied, bold, regular, "Stream / Course",      nvl(app.getStream()));
        addRow(applied, bold, regular, "Educational Status",   nvl(app.getEducationalStatus()));
        doc.add(applied);

        // ── Section 5 – Community & Financial Status ──────────────────────────
        doc.add(sectionHeader("5. Community & Financial Status", bold));
        Table comm = dataTable();
        addRow(comm, bold, regular, "Community",               nvl(app.getCommunity()));
        addRow(comm, bold, regular, "Sub Caste",               nvl(app.getSubCaste()));
        addRow(comm, bold, regular, "Annual Income Below Limit",
                app.getIncomeBelowThreshold() != null
                        ? (app.getIncomeBelowThreshold() ? "YES" : "NO") : "");
        doc.add(comm);

        // ── Section 6 – College Preferences ──────────────────────────────────
        doc.add(sectionHeader("6. College Preferences", bold));
        Table prefs = dataTable();
        if (priorities.isEmpty()) {
            addRow(prefs, bold, regular, "—", "No preferences recorded");
        } else {
            for (CollegePriority cp : priorities) {
                addRow(prefs, bold, regular,
                        "Priority " + cp.getPriorityOrder(),
                        nvl(cp.getCollegeName()));
            }
        }
        doc.add(prefs);

        // ── Section 7 – Payment Details ───────────────────────────────────────
        doc.add(sectionHeader("7. Payment Details", bold));
        Table pay = dataTable();
        if (payOpt.isPresent()) {
            Payment p = payOpt.get();
            // Status with colour indicator
            String statusText = p.getStatus().name();
            Color  statusColor = p.getStatus() == Payment.PaymentStatus.PAID
                    ? PAID_GREEN : PENDING_ORANGE;

            addRow(pay, bold, regular, "Payment Status",   statusText);
            addRow(pay, bold, regular, "Amount",
                    p.getAmount() != null
                            ? "\u20B9 " + String.format("%.2f", p.getAmount()) : "—");
            addRow(pay, bold, regular, "Transaction ID",
                    hasContent(p.getTransactionId()) ? p.getTransactionId() : "—");
            addRow(pay, bold, regular, "Payment Gateway",
                    hasContent(p.getGateway()) ? p.getGateway() : "—");
            addRow(pay, bold, regular, "Bank Reference No.",
                    hasContent(p.getBankRef()) ? p.getBankRef() : "—");
            addRow(pay, bold, regular, "Payment Date",
                    p.getPaidAt() != null ? p.getPaidAt().format(DT_FMT) : "—");
            addRow(pay, bold, regular, "Initiated At",
                    p.getInitiatedAt() != null ? p.getInitiatedAt().format(DT_FMT) : "—");

            // Highlighted status banner
            doc.add(pay);
            Table statusBanner = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginTop(2);
            statusBanner.addCell(new Cell()
                    .setBackgroundColor(p.getStatus() == Payment.PaymentStatus.PAID
                            ? new DeviceRgb(200, 230, 201) : new DeviceRgb(255, 224, 178))
                    .setBorder(new SolidBorder(statusColor, 1))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(para(
                            p.getStatus() == Payment.PaymentStatus.PAID
                                ? "✔  Application Fee PAID — Application Confirmed"
                                : "⚠  Application Fee PENDING — Pay ₹100 to confirm your application",
                            bold, 9, statusColor)));
            doc.add(statusBanner);
        } else {
            addRow(pay, bold, regular, "Payment Status", "Not Initiated");
            addRow(pay, bold, regular, "Amount",         "\u20B9 100.00");
            doc.add(pay);
            Table statusBanner = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginTop(2);
            statusBanner.addCell(new Cell()
                    .setBackgroundColor(new DeviceRgb(255, 224, 178))
                    .setBorder(new SolidBorder(PENDING_ORANGE, 1))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(para("⚠  Application Fee PENDING — Pay ₹100 to confirm your application",
                            bold, 9, PENDING_ORANGE)));
            doc.add(statusBanner);
        }

        // ── Signature ─────────────────────────────────────────────────────────
        if (hasContent(app.getSignatureBase64())) {
            try {
                doc.add(sectionHeader("Candidate Signature", bold));
                byte[] sigBytes = decodeBase64Image(app.getSignatureBase64());
                Image  sig      = new Image(ImageDataFactory.create(sigBytes))
                        .setWidth(160).setHeight(45);
                doc.add(sig);
            } catch (Exception ignored) {}
        }

        // ── Declaration box ───────────────────────────────────────────────────
        doc.add(new Paragraph("\n"));
        Table declBox = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginTop(4);
        declBox.addCell(new Cell()
                .setBorder(new SolidBorder(ROW_BORDER, 1))
                .setBackgroundColor(GREEN_BG)
                .setPadding(6)
                .add(para("Declaration: I hereby declare that all information provided in this application is "
                        + "true and correct to the best of my knowledge. I understand that providing false "
                        + "information may result in cancellation of my admission.", regular, 7, GREEN_DARK)));
        doc.add(declBox);

        // ── Footer ────────────────────────────────────────────────────────────
        doc.add(new Paragraph("\n"));
        doc.add(para("This is a computer-generated application. Signature of Candidate: _________________________",
                regular, 7, ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
        doc.add(para("TTWREIS Admission Portal 2025-26 | Government of Telangana",
                regular, 7, ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Loads an image from src/main/resources/static/.
     * Returns null (gracefully) if the file is not found — PDF still generates with text fallback.
     */
    private byte[] loadStaticImage(String resourcePath) {
        try {
            ClassPathResource res = new ClassPathResource(resourcePath);
            if (!res.exists()) return null;
            try (InputStream is = res.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Table sectionHeader(String title, PdfFont bold) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(8).setMarginBottom(2);
        t.addCell(new Cell()
                .setBackgroundColor(GREEN_DARK)
                .setPadding(4)
                .setBorder(Border.NO_BORDER)
                .add(para(title, bold, 9, ColorConstants.WHITE)));
        return t;
    }

    private Table dataTable() {
        return new Table(UnitValue.createPercentArray(new float[]{2, 3}))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private void addRow(Table table, PdfFont bold, PdfFont regular, String label, String value) {
        Border b = new SolidBorder(ROW_BORDER, 0.5f);
        table.addCell(new Cell().setBorder(b).setBackgroundColor(GREEN_BG)
                .setPadding(3).add(para(label, bold, 8, GREEN_DARK)));
        table.addCell(new Cell().setBorder(b)
                .setPadding(3).add(para(value, regular, 8, ColorConstants.BLACK)));
    }

    private Cell noBorderCell() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    private Paragraph para(String text, PdfFont font, float size, Color color) {
        return new Paragraph(text).setFont(font).setFontSize(size)
                .setFontColor(color).setMargin(0);
    }

    private String nvl(String v)         { return v != null ? v : ""; }
    private boolean hasContent(String s) { return s != null && !s.isBlank(); }

    private byte[] decodeBase64Image(String base64) {
        String clean = base64.replaceFirst("^data:image/[^;]+;base64,", "");
        return Base64.getDecoder().decode(clean);
    }
}