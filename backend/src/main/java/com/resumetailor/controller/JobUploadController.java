package com.resumetailor.controller;

import com.resumetailor.model.Job;
import com.resumetailor.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
@CrossOrigin(origins = "*")
public class JobUploadController {

    private static final Logger log = LoggerFactory.getLogger(JobUploadController.class);

    @Autowired
    private JobRepository jobRepository;

    @PostMapping(value = "/upload", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> uploadJobDescription(
            @RequestBody String jobDescriptionText,
            @RequestParam(value = "title", required = false, defaultValue = "Job Position") String title,
            @RequestParam(value = "company", required = false, defaultValue = "Company") String company,
            @RequestParam(value = "location", required = false, defaultValue = "") String location,
            @RequestParam(value = "employmentType", required = false, defaultValue = "Full-time") String employmentType,
            @RequestParam(value = "experienceLevel", required = false, defaultValue = "Mid-level") String experienceLevel) {

        try {
            if (jobDescriptionText == null || jobDescriptionText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Job description cannot be empty");
            }


            Job job = new Job();
            job.setTitle(title);
            job.setCompany(company);
            job.setLocation(location);
            job.setDescription(jobDescriptionText.trim());
            job.setRequirements("");
            job.setSalaryRange("");
            job.setEmploymentType(employmentType);
            job.setExperienceLevel(experienceLevel);
            job.setJobUrl("");
            job.setPostedDate(LocalDateTime.now());
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job.setApplicationDeadline(null);

            Job savedJob = jobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Job description uploaded successfully");
            response.put("jobId", savedJob.getId());
            response.put("title", title);
            response.put("company", company);
            response.put("textLength", jobDescriptionText.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading job description: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to process job description: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(job -> ResponseEntity.ok().body(job))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return ResponseEntity.ok().body("Job description deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}