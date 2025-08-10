package com.resumetailor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailoredResumeResponse {
    private Long id;
    private Long resumeId;
    private Long jobId;
    private int atsScore;
    private String tailoredText;
    private String candidateName;
    private String jobTitle;
    private LocalDateTime createdAt;
}