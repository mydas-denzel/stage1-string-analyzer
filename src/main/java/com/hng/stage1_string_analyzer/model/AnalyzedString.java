package com.hng.stage1_string_analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzedString {
    @Id
    private String id; // sha256 hash

    @Column(unique = true, nullable = false)
    private String value;

    private int length;
    private boolean isPalindrome;
    private int uniqueCharacters;
    private int wordCount;

    @Convert(converter = CharacterFrequencyMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> characterFrequencyMap;

    private Instant createdAt;
}
