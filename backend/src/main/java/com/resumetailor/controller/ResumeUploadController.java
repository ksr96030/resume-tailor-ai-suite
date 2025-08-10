package com.resumetailor.controller;

import com.resumetailor.model.Resume;
import com.resumetailor.repository.ResumeRepository;
import com.resumetailor.service.FileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeUploadController {

    private static final Logger log = LoggerFactory.getLogger(ResumeUploadController.class);

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private FileProcessingService fileProcessingService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "candidateName", required = false, defaultValue = "Unknown") String candidateName,
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            @RequestParam(value = "phone", required = false, defaultValue = "") String phone) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Extract text from file
            String extractedText = fileProcessingService.extractTextFromFile(file);

            // Save resume with all required fields
            Resume resume = new Resume();
            resume.setContent(extractedText);
            resume.setFilename(file.getOriginalFilename());
            resume.setCandidateName(candidateName);
            resume.setEmail(email);
            resume.setPhone(phone);
            resume.setFileType(file.getContentType());
            resume.setFileSize(file.getSize());
            resume.setUploadedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());

            Resume savedResume = resumeRepository.save(resume);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Resume uploaded successfully");
            response.put("resumeId", savedResume.getId());
            response.put("candidateName", candidateName);
            response.put("extractedLength", extractedText.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading resume: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to process resume: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<List<Resume>> getAllResumes() {
        List<Resume> resumes = resumeRepository.findAllByOrderByUploadedAtDesc();
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable Long id) {
        return resumeRepository.findById(id)
                .map(resume -> ResponseEntity.ok().body(resume))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id) {
        if (resumeRepository.existsById(id)) {
            resumeRepository.deleteById(id);
            return ResponseEntity.ok().body("Resume deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    private boolean isValidFileType(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx");
    }
}