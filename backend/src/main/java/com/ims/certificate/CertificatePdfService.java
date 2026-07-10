package com.ims.certificate;

import com.ims.common.NotFoundException;
import com.ims.institute.Institute;
import com.ims.institute.InstituteRepository;
import com.ims.people.Student;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** Renders a certificate to a nicely framed A4-landscape PDF. */
@Service
public class CertificatePdfService {

    private static final Color INK = new Color(30, 41, 59);       // slate-800
    private static final Color ACCENT = new Color(79, 70, 229);   // indigo-600
    private static final Color MUTED = new Color(100, 116, 139);  // slate-500
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final InstituteRepository instituteRepository;

    public CertificatePdfService(CertificateRepository certificateRepository,
                                 StudentRepository studentRepository,
                                 InstituteRepository instituteRepository) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.instituteRepository = instituteRepository;
    }

    public record Rendered(byte[] bytes, String fileName) {
    }

    @Transactional(readOnly = true)
    public Rendered render(UUID id) {
        Certificate cert = certificateRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Certificate not found"));
        Student student = studentRepository.findById(cert.getStudentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        String instituteName = instituteRepository.findById(cert.getInstituteId())
                .map(Institute::getName).orElse("Institute");

        byte[] bytes = build(cert, student.getFullName(), instituteName);
        String serial = cert.getSerialNo() != null ? cert.getSerialNo() : id.toString().substring(0, 8);
        return new Rendered(bytes, "certificate-" + serial + ".pdf");
    }

    private byte[] build(Certificate cert, String studentName, String instituteName) {
        Document doc = new Document(PageSize.A4.rotate(), 48, 48, 48, 48);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();

        drawFrame(writer, doc);

        Font instituteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, INK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, ACCENT);
        Font typeFont = FontFactory.getFont(FontFactory.HELVETICA, 12, MUTED);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 13, INK);
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, ACCENT);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);

        doc.add(spaced(new Paragraph(instituteName.toUpperCase(), instituteFont), 24, 6));
        doc.add(centered(new Paragraph("CERTIFICATE", labelFont)));
        doc.add(spaced(centered(new Paragraph(prettyType(cert.getType()), typeFont)), 0, 20));

        doc.add(spaced(centered(new Paragraph("This is to certify that", bodyFont)), 6, 10));
        doc.add(centered(new Paragraph(studentName, nameFont)));

        String body = cert.getContent() != null && !cert.getContent().isBlank()
                ? cert.getContent()
                : defaultBody(cert.getType());
        Paragraph bodyPara = centered(new Paragraph(body, bodyFont));
        doc.add(spaced(bodyPara, 18, 6));

        Paragraph title = centered(new Paragraph(cert.getTitle(), bodyFont));
        doc.add(spaced(title, 10, 30));

        // Footer: serial / date / signature
        Font footFont = smallFont;
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        String serialTxt = cert.getSerialNo() != null ? "Serial: " + cert.getSerialNo() : "";
        String dateTxt = cert.getIssueDate() != null ? "Issued: " + cert.getIssueDate().format(DATE) : "";
        footer.add(new Phrase(serialTxt + (serialTxt.isEmpty() ? "" : "        ") + dateTxt, footFont));
        doc.add(spaced(footer, 40, 0));

        Paragraph sign = new Paragraph("__________________________\nAuthorized Signature", smallFont);
        sign.setAlignment(Element.ALIGN_RIGHT);
        doc.add(spaced(sign, 24, 0));

        doc.close();
        return out.toByteArray();
    }

    private void drawFrame(PdfWriter writer, Document doc) {
        Rectangle page = doc.getPageSize();
        PdfContentByte cb = writer.getDirectContent();
        // Outer thick accent border
        cb.setColorStroke(ACCENT);
        cb.setLineWidth(3f);
        cb.rectangle(28, 28, page.getWidth() - 56, page.getHeight() - 56);
        cb.stroke();
        // Inner thin border
        cb.setColorStroke(MUTED);
        cb.setLineWidth(0.8f);
        cb.rectangle(38, 38, page.getWidth() - 76, page.getHeight() - 76);
        cb.stroke();
    }

    private static Paragraph centered(Paragraph p) {
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private static Paragraph spaced(Paragraph p, float before, float after) {
        p.setSpacingBefore(before);
        p.setSpacingAfter(after);
        return p;
    }

    private static String prettyType(CertificateType type) {
        String s = type.name().toLowerCase().replace('_', ' ');
        return "of " + Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String defaultBody(CertificateType type) {
        return switch (type) {
            case TRANSFER -> "has been granted a transfer certificate from this institution.";
            case CHARACTER -> "bears a good moral character during the period of study.";
            case COMPLETION -> "has successfully completed the prescribed course of study.";
            case PARTICIPATION -> "participated actively and is awarded this certificate.";
            case MARKSHEET -> "is issued this statement of marks for the examination.";
            default -> "is awarded this certificate.";
        };
    }
}
