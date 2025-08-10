package com.resumetailor.repository;

import com.resumetailor.model.TailoredResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TailoredResumeRepository extends JpaRepository<TailoredResume, Long> {

    List<TailoredResume> findAllByOrderByCreatedAtDesc();

    @Query("SELECT tr FROM TailoredResume tr WHERE tr.resume.id = :resumeId ORDER BY tr.createdAt DESC")
    List<TailoredResume> findByResumeIdOrderByCreatedAtDesc(Long resumeId);

    @Query("SELECT tr FROM TailoredResume tr WHERE tr.job.id = :jobId ORDER BY tr.createdAt DESC")
    List<TailoredResume> findByJobIdOrderByCreatedAtDesc(Long jobId);
}