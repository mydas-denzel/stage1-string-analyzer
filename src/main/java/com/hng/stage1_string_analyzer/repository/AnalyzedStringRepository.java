package com.hng.stage1_string_analyzer.repository;

import com.hng.stage1_string_analyzer.model.AnalyzedString;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalyzedStringRepository extends JpaRepository<AnalyzedString, String> {
    Optional<AnalyzedString> findByValue(String value);
    void deleteByValue(String value);
    boolean existsByValue(String value);
}
