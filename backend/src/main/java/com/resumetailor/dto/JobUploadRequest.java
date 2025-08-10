package com.resumetailor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobUploadRequest {
    private String title;
    private String company;
    private String location;
    private String description;
    private String requirements;
    private String salaryRange;
    private String employmentType;
    private String experienceLevel;
    private String jobUrl;
}