package com.resumetailor.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PDFService {
    private static final Logger log = LoggerFactory.getLogger(PDFService.class);

    // Color constants
    private static final BaseColor HEADER_COLOR = new BaseColor(51, 51, 51); // Dark gray
    private static final BaseColor SECTION_COLOR = new BaseColor(102, 102, 102); // Medium gray
    private static final BaseColor TEXT_COLOR = BaseColor.BLACK;

    public byte[] generateResumePDF(String resumeText, String candidateName) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50); // margins
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            // Process and add content
            processResumeContent(document, resumeText, candidateName);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void processResumeContent(Document document, String resumeText, String candidateName)
            throws DocumentException {

        String[] lines = resumeText.split("\n");
        boolean isFirstLine = true;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                // Add spacing between sections
                document.add(new Paragraph(" ", getFont(FontType.CONTENT)));
                continue;
            }

            // Handle different types of content
            if (isFirstLine && isContactHeader(line)) {
                addContactHeader(document, line);
                isFirstLine = false;
            } else if (isSectionHeader(line)) {
                addSectionHeader(document, line);
            } else if (isBulletPoint(line)) {
                addBulletPoint(document, line);
            } else if (isSubBulletPoint(line)) {
                addSubBulletPoint(document, line);
            } else if (isJobTitle(line, i < lines.length - 1 ? lines[i + 1] : "")) {
                addJobTitle(document, line);
            } else {
                addRegularText(document, line);
            }
        }
    }

    private void addContactHeader(Document document, String line) throws DocumentException {
        // Split contact info by | or newline
        String[] parts = line.split("\\|");

        // Name (first part, likely contains name)
        String name = parts[0].trim();
        Font nameFont = getFont(FontType.NAME);
        Paragraph namePara = new Paragraph(name, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        namePara.setSpacingAfter(10);
        document.add(namePara);

        // Contact details
        if (parts.length > 1) {
            StringBuilder contactInfo = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) contactInfo.append(" | ");
                contactInfo.append(parts[i].trim());
            }

            Font contactFont = getFont(FontType.CONTACT);
            Paragraph contactPara = new Paragraph(contactInfo.toString(), contactFont);
            contactPara.setAlignment(Element.ALIGN_CENTER);
            contactPara.setSpacingAfter(20);
            document.add(contactPara);
        }
    }

    private void addSectionHeader(Document document, String line) throws DocumentException {
        Font headerFont = getFont(FontType.SECTION);
        Paragraph paragraph = new Paragraph(line.toUpperCase(), headerFont);
        paragraph.setSpacingBefore(15);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);

        // Add a subtle line under section headers
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(SECTION_COLOR);
        separator.setLineWidth(0.5f);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" ", getFont(FontType.CONTENT))); // Small spacing
    }

    private void addJobTitle(Document document, String line) throws DocumentException {
        Font jobFont = getFont(FontType.JOB_TITLE);
        Paragraph paragraph = new Paragraph(line, jobFont);
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(5);
        document.add(paragraph);
    }

    private void addBulletPoint(Document document, String line) throws DocumentException {
        // Clean the bullet point
        String content = line.replaceFirst("^[•\\*-]\\s*", "").trim();

        Font bulletFont = getFont(FontType.CONTENT);
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk("• ", bulletFont));
        paragraph.add(new Chunk(content, bulletFont));
        paragraph.setSpacingAfter(4);
        paragraph.setIndentationLeft(20);
        document.add(paragraph);
    }

    private void addSubBulletPoint(Document document, String line) throws DocumentException {
        // Clean the sub-bullet point
        String content = line.replaceFirst("^\\s*[◦+]\\s*", "").trim();

        Font bulletFont = getFont(FontType.CONTENT);
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk("  ◦ ", bulletFont));
        paragraph.add(new Chunk(content, bulletFont));
        paragraph.setSpacingAfter(3);
        paragraph.setIndentationLeft(40);
        document.add(paragraph);
    }

    private void addRegularText(Document document, String line) throws DocumentException {
        Font contentFont = getFont(FontType.CONTENT);
        Paragraph paragraph = new Paragraph(line, contentFont);
        paragraph.setSpacingAfter(5);
        document.add(paragraph);
    }

    // Helper methods for content detection
    private boolean isContactHeader(String line) {
        return line.contains("|") || line.matches(".*\\+\\d.*") || line.contains("@");
    }

    private boolean isSectionHeader(String line) {
        String upper = line.toUpperCase();
        String[] headers = {"SUMMARY", "OBJECTIVE", "EXPERIENCE", "EDUCATION", "SKILLS",
                "PROJECTS", "CERTIFICATIONS", "ACHIEVEMENTS", "PROFESSIONAL EXPERIENCE"};

        for (String header : headers) {
            if (upper.contains(header)) {
                return true;
            }
        }

        // Check if line is all uppercase and short (likely a header)
        return line.equals(upper) && line.length() < 100 && !line.contains(".");
    }

    private boolean isBulletPoint(String line) {
        return line.matches("^\\s*[•\\*-]\\s+.*");
    }

    private boolean isSubBulletPoint(String line) {
        return line.matches("^\\s{2,}[◦+]\\s+.*");
    }

    private boolean isJobTitle(String line, String nextLine) {
        // Check if line contains job title patterns
        return line.contains(",") &&
                (line.contains("| ") || line.matches(".*\\d{4}\\s*-.*")) &&
                !line.startsWith("•") && !line.startsWith("*");
    }

    // Font management
    private enum FontType {
        NAME, CONTACT, SECTION, JOB_TITLE, CONTENT
    }

    private Font getFont(FontType type) {
        switch (type) {
            case NAME:
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, HEADER_COLOR);
            case CONTACT:
                return FontFactory.getFont(FontFactory.HELVETICA, 10, SECTION_COLOR);
            case SECTION:
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, HEADER_COLOR);
            case JOB_TITLE:
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_COLOR);
            case CONTENT:
            default:
                return FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_COLOR);
        }
    }
}