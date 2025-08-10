package com.resumetailor.repository;

import com.resumetailor.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findAllByOrderByUploadedAtDesc();

    List<Resume> findByCandidateNameContainingIgnoreCase(String candidateName);

    @Query("SELECT r FROM Resume r WHERE r.email = :email")
    List<Resume> findByEmail(String email);
}