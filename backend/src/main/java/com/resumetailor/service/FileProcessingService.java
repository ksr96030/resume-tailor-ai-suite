package com.resumetailor.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileProcessingService {

    public String extractTextFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String lowercaseFilename = filename.toLowerCase();

            if (lowercaseFilename.endsWith(".pdf") || "application/pdf".equals(contentType)) {
                return extractFromPDF(inputStream);
            } else if (lowercaseFilename.endsWith(".txt") || "text/plain".equals(contentType)) {
                return extractFromText(inputStream);
            } else if (lowercaseFilename.endsWith(".doc") || lowercaseFilename.endsWith(".docx")) {
                return extractFromWord(inputStream);
            } else {
                throw new UnsupportedOperationException("Unsupported file type. Supported: PDF, TXT, DOC, DOCX");
            }
        }
    }

    private String extractFromPDF(InputStream inputStream) throws IOException {
        byte[] pdfBytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("[FileProcessing] Extracted {} characters from PDF", text.length());
            return text;
        }
    }

    private String extractFromText(InputStream inputStream) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        log.info("[FileProcessing] Extracted {} characters from TXT", text.length());
        return text;
    }

    private String extractFromWord(InputStream inputStream) throws IOException {
        // Simple text extraction - you can implement this based on your needs
        // For now, throw exception to indicate it's not implemented
        throw new UnsupportedOperationException("Word document processing not yet implemented");
    }
}