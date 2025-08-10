package com.resumetailor.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PDFService {
    private static final Logger log = LoggerFactory.getLogger(PDFService.class);

    public byte[] generateResumePDF(String resumeText, String candidateName) {
        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph(candidateName != null ? candidateName + "  Resume" : "Tailored Resume", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add content
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

            // Split content into paragraphs and format
            String[] sections = resumeText.split("\n\n");
            for (String section : sections) {
                if (section.trim().isEmpty()) continue;

                // Check if it's a section header (all caps or contains specific keywords)
                if (isHeader(section)) {
                    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
                    Paragraph header = new Paragraph(section.trim(), headerFont);
                    header.setSpacingBefore(15);
                    header.setSpacingAfter(5);
                    document.add(header);
                } else {
                    // Regular content
                    String[] lines = section.split("\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        Paragraph paragraph = new Paragraph(line.trim(), contentFont);
                        paragraph.setSpacingAfter(3);
                        document.add(paragraph);
                    }
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private boolean isHeader(String text) {
        String trimmed = text.trim().toUpperCase();
        return trimmed.equals(text.trim()) ||
                trimmed.contains("EXPERIENCE") ||
                trimmed.contains("EDUCATION") ||
                trimmed.contains("SKILLS") ||
                trimmed.contains("CONTACT") ||
                trimmed.contains("SUMMARY") ||
                trimmed.contains("OBJECTIVE") ||
                (text.trim().length() < 50 && !text.contains("."));
    }
}