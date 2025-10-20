package com.hng.stage1_string_analyzer.service;


import com.hng.stage1_string_analyzer.model.AnalyzedString;
import com.hng.stage1_string_analyzer.repository.AnalyzedStringRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StringAnalysisService {

    private final AnalyzedStringRepository repository;

    public StringAnalysisService(AnalyzedStringRepository repository) {
        this.repository = repository;
    }

    public AnalyzedString analyzeAndSave(String value) {
        if (repository.existsByValue(value)) {
            throw new IllegalArgumentException("String already exists");
        }

        String id = sha256(value);
        Map<String, Integer> freq = getCharFrequency(value);
        boolean palindrome = isPalindrome(value);

        AnalyzedString analyzed = AnalyzedString.builder()
                .id(id)
                .value(value)
                .length(value.length())
                .isPalindrome(palindrome)
                .uniqueCharacters(freq.size())
                .wordCount(value.trim().isEmpty() ? 0 : value.trim().split("\\s+").length)
                .characterFrequencyMap(freq)
                .createdAt(Instant.now())
                .build();

        return repository.save(analyzed);
    }

    public Optional<AnalyzedString> getByValue(String value) {
        return repository.findByValue(value);
    }

    public List<AnalyzedString> filter(
            Boolean isPalindrome, Integer minLength, Integer maxLength,
            Integer wordCount, String containsCharacter) {

        return repository.findAll().stream()
                .filter(a -> isPalindrome == null || a.isPalindrome() == isPalindrome)
                .filter(a -> minLength == null || a.getLength() >= minLength)
                .filter(a -> maxLength == null || a.getLength() <= maxLength)
                .filter(a -> wordCount == null || a.getWordCount() == wordCount)
                .filter(a -> containsCharacter == null || a.getCharacterFrequencyMap().containsKey(containsCharacter))
                .collect(Collectors.toList());
    }

    public void deleteByValue(String value) {
        if (!repository.existsByValue(value)) {
            throw new NoSuchElementException("String not found");
        }
        repository.deleteByValue(value);
    }

    private Map<String, Integer> getCharFrequency(String input) {
        Map<String, Integer> freq = new LinkedHashMap<>();
        for (char c : input.toCharArray()) {
            freq.merge(String.valueOf(c), 1, Integer::sum);
        }
        return freq;
    }

    private boolean isPalindrome(String value) {
        String lower = value.toLowerCase();
        return lower.equals(new StringBuilder(lower).reverse().toString());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA256 computation failed");
        }
    }
}
