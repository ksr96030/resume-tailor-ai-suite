package com.resumetailor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company")
    private String company;

    @Column(name = "location")
    private String location;

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "salary_range")
    private String salaryRange;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;

    @Column(name = "job_url")
    private String jobUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-many relationship with TailoredResume
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TailoredResume> tailoredResumes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}