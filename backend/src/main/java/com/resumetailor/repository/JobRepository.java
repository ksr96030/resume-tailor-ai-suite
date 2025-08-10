package com.resumetailor.repository;

import com.resumetailor.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByOrderByCreatedAtDesc();

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByCompanyContainingIgnoreCase(String company);

    @Query("SELECT j FROM Job j WHERE j.title LIKE %:keyword% OR j.description LIKE %:keyword%")
    List<Job> searchByKeyword(String keyword);
}