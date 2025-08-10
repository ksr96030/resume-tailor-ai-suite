package com.resumetailor.controller;

import com.resumetailor.dto.TailorResumeRequest;
import com.resumetailor.dto.TailoredResumeResponse;
import com.resumetailor.model.Job;
import com.resumetailor.model.Resume;
import com.resumetailor.model.TailoredResume;
import com.resumetailor.repository.JobRepository;
import com.resumetailor.repository.ResumeRepository;
import com.resumetailor.repository.TailoredResumeRepository;
import com.resumetailor.service.AIService;
import com.resumetailor.service.ATSService;
import com.resumetailor.service.PDFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TailoredResumeRepository tailoredResumeRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ATSService atsService;

    @Autowired
    private PDFService pdfService;

    @PostMapping("/tailor")
    public ResponseEntity<TailoredResumeResponse> tailorResume(@RequestBody TailorResumeRequest request) {
        log.info("[ResumeController] Enhanced tailoring resume {} for job {}", request.getResumeId(), request.getJobId());

        try {
            Optional<Resume> resumeOpt = resumeRepository.findById(request.getResumeId());
            Optional<Job> jobOpt = jobRepository.findById(request.getJobId());

            if (resumeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        TailoredResumeResponse.builder()
                                .resumeId(request.getResumeId())
                                .jobId(request.getJobId())
                                .atsScore(0)
                                .tailoredText("Resume not found")
                                .build()
                );
            }

            if (jobOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        TailoredResumeResponse.builder()
                                .resumeId(request.getResumeId())
                                .jobId(request.getJobId())
                                .atsScore(0)
                                .tailoredText("Job not found")
                                .build()
                );
            }

            Resume resume = resumeOpt.get();
            Job job = jobOpt.get();

            log.info("[ResumeController] Processing - Resume: {} chars, Job: {} chars",
                    resume.getContent().length(), job.getDescription().length());

            // Generate tailored resume using enhanced AI
            String rawTailoredText = aiService.generateTailoredResume(resume.getContent(), job.getDescription());

            // Clean and format the tailored text
            String tailoredText = cleanAndFormatResumeText(rawTailoredText);

            // Calculate ATS score using existing service
            int atsScore = atsService.calculateATSScore(tailoredText, job.getDescription());

            log.info("[ResumeController] Tailoring complete - Original: {} chars, Tailored: {} chars, ATS Score: {}",
                    resume.getContent().length(), tailoredText.length(), atsScore);

            // Save tailored resume
            TailoredResume tailoredResume = TailoredResume.builder()
                    .resume(resume)
                    .job(job)
                    .tailoredContent(tailoredText)
                    .atsScore(atsScore)
                    .createdAt(LocalDateTime.now())
                    .build();

            tailoredResume = tailoredResumeRepository.save(tailoredResume);

            return ResponseEntity.ok(
                    TailoredResumeResponse.builder()
                            .id(tailoredResume.getId())
                            .resumeId(request.getResumeId())
                            .jobId(request.getJobId())
                            .atsScore(atsScore)
                            .tailoredText(tailoredText)
                            .candidateName(resume.getCandidateName())
                            .jobTitle(job.getTitle())
                            .createdAt(tailoredResume.getCreatedAt())
                            .build()
            );

        } catch (Exception e) {
            log.error("[ResumeController] Error tailoring resume: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    TailoredResumeResponse.builder()
                            .resumeId(request.getResumeId())
                            .jobId(request.getJobId())
                            .atsScore(0)
                            .tailoredText("Error: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Cleans and formats the resume text by removing unwanted notes and converting markdown
     */
    private String cleanAndFormatResumeText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return rawText;
        }

        // Remove the "Note:" section and everything after it
        String cleanedText = rawText;

        // Find and remove note sections (case insensitive)
        String[] notePatterns = {
                "(?i)\\n\\s*Note:.*$",
                "(?i)\\n\\s*Note -.*$",
                "(?i)\\n\\s*\\*\\*Note\\*\\*:.*$",
                "(?i)\\n\\s*I have rewritten.*$"
        };

        for (String pattern : notePatterns) {
            cleanedText = cleanedText.replaceAll(pattern, "");
        }

        // Convert markdown formatting to plain text
        cleanedText = convertMarkdownToPlainText(cleanedText);

        // Clean up extra whitespace
        cleanedText = cleanedText.replaceAll("\\n{3,}", "\n\n"); // Replace 3+ newlines with 2
        cleanedText = cleanedText.trim();

        return cleanedText;
    }

    /**
     * Converts markdown formatting to plain text suitable for PDF generation
     */
    private String convertMarkdownToPlainText(String text) {
        // Remove markdown bold formatting
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // Handle bullet points - convert * to •
        text = text.replaceAll("^\\s*\\*\\s+", "• ");
        text = text.replaceAll("\\n\\s*\\*\\s+", "\n• ");

        // Handle sub-bullet points - convert + to ◦
        text = text.replaceAll("^\\s*\\+\\s+", "  ◦ ");
        text = text.replaceAll("\\n\\s*\\+\\s+", "\n  ◦ ");

        // Handle numbered lists
        text = text.replaceAll("\\n\\s*\\d+\\.\\s+", "\n• ");

        // Remove extra markdown syntax
        text = text.replaceAll("\\*([^*]+)\\*", "$1"); // Remove single asterisks
        text = text.replaceAll("`([^`]+)`", "$1"); // Remove backticks
        text = text.replaceAll("#+\\s*", ""); // Remove heading markers

        return text;
    }

    // New ATS Score API
    @PostMapping("/ats-score")
    public ResponseEntity<?> calculateATSScore(@RequestBody TailorResumeRequest request) {
        log.info("[ResumeController] Calculating ATS score for resume {} and job {}",
                request.getResumeId(), request.getJobId());

        try {
            Optional<Resume> resumeOpt = resumeRepository.findById(request.getResumeId());
            Optional<Job> jobOpt = jobRepository.findById(request.getJobId());

            if (resumeOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Resume not found");
                return ResponseEntity.badRequest().body(error);
            }

            if (jobOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Job not found");
                return ResponseEntity.badRequest().body(error);
            }

            Resume resume = resumeOpt.get();
            Job job = jobOpt.get();

            // Calculate basic ATS score using existing service
            int basicScore = atsService.calculateATSScore(resume.getContent(), job.getDescription());

            // Get detailed ATS analysis from AI (if available)
            Map<String, Object> detailedAnalysis = aiService.calculateATSScoreWithAI(
                    resume.getContent(), job.getDescription());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ATS score calculated successfully");
            response.put("resumeId", request.getResumeId());
            response.put("jobId", request.getJobId());
            response.put("candidateName", resume.getCandidateName());
            response.put("jobTitle", job.getTitle());
            response.put("basicScore", basicScore);
            response.put("detailedScore", detailedAnalysis.get("score"));
            response.put("breakdown", detailedAnalysis.get("breakdown"));
            response.put("matchingKeywords", detailedAnalysis.get("matchingKeywords"));
            response.put("missingKeywords", detailedAnalysis.get("missingKeywords"));
            response.put("suggestions", detailedAnalysis.get("suggestions"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[ResumeController] Error calculating ATS score: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to calculate ATS score: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/tailored/{id}")
    public ResponseEntity<TailoredResumeResponse> getTailoredResume(@PathVariable Long id) {
        Optional<TailoredResume> tailoredOpt = tailoredResumeRepository.findById(id);

        if (tailoredOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TailoredResume tailored = tailoredOpt.get();
        return ResponseEntity.ok(
                TailoredResumeResponse.builder()
                        .id(tailored.getId())
                        .resumeId(tailored.getResume().getId())
                        .jobId(tailored.getJob().getId())
                        .atsScore(tailored.getAtsScore())
                        .tailoredText(tailored.getTailoredContent())
                        .candidateName(tailored.getResume().getCandidateName())
                        .jobTitle(tailored.getJob().getTitle())
                        .createdAt(tailored.getCreatedAt())
                        .build()
        );
    }

    @GetMapping("/tailored")
    public ResponseEntity<List<TailoredResume>> getAllTailoredResumes() {
        List<TailoredResume> tailoredResumes = tailoredResumeRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(tailoredResumes);
    }

    @GetMapping("/tailored/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadTailoredResume(@PathVariable Long id) {
        Optional<TailoredResume> tailoredOpt = tailoredResumeRepository.findById(id);

        if (tailoredOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            TailoredResume tailored = tailoredOpt.get();
            String candidateName = tailored.getResume().getCandidateName();
            String jobTitle = tailored.getJob().getTitle();

            byte[] pdfBytes = pdfService.generateResumePDF(tailored.getTailoredContent(), candidateName);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            String filename = String.format("%s_Resume_%s.pdf",
                    candidateName != null ? candidateName.replaceAll("[^a-zA-Z0-9]", "_") : "Resume",
                    jobTitle != null ? jobTitle.replaceAll("[^a-zA-Z0-9]", "_") : "Tailored"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            log.error("[ResumeController] Error generating PDF: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private int calculateATSScore(String tailoredText, String jobDescription) {
        // Simple ATS score calculation based on keyword matching
        String[] jobKeywords = jobDescription.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .split("\\s+");

        String tailoredLower = tailoredText.toLowerCase();
        int matches = 0;
        int totalKeywords = 0;

        for (String keyword : jobKeywords) {
            if (keyword.length() > 3) { // Only consider words longer than 3 characters
                totalKeywords++;
                if (tailoredLower.contains(keyword)) {
                    matches++;
                }
            }
        }

        return totalKeywords > 0 ? (int) ((matches * 100.0) / totalKeywords) : 0;
    }
}