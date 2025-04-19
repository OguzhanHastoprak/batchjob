package com.example.batchJob.repository;

import com.example.batchJob.model.ApiDataFetch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiDataFetchRepository extends JpaRepository<ApiDataFetch, Long> {

    Optional<ApiDataFetch> findTopByOrderByFetchedAtDesc();
}
