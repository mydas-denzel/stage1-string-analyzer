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

    // Existing filter signature kept for compatibility (AND semantics)
    public List<AnalyzedString> filter(
            Boolean isPalindrome, Integer minLength, Integer maxLength,
            Integer wordCount, String containsCharacter) {

        return repository.findAll().stream()
                .filter(a -> isPalindrome == null || a.isPalindrome() == isPalindrome)
                .filter(a -> minLength == null || a.getLength() >= minLength)
                .filter(a -> maxLength == null || a.getLength() <= maxLength)
                .filter(a -> wordCount == null || a.getWordCount() == wordCount)
                .filter(a -> containsCharacter == null || containsCharacter.isEmpty()
                        || containsCharacterMatches(a, containsCharacter))
                .collect(Collectors.toList());
    }

    // NEW: accepts parsed filters (snake_case) directly
    public List<AnalyzedString> filter(Map<String, Object> parsedFilters) {
        // fetch all and apply filters step by step
        List<AnalyzedString> all = repository.findAll();

        if (parsedFilters == null || parsedFilters.isEmpty()) {
            return all;
        }

        List<AnalyzedString> result = all.stream()
                .filter(a -> {
                    // is_palindrome
                    if (parsedFilters.containsKey("is_palindrome")) {
                        Boolean want = (Boolean) parsedFilters.get("is_palindrome");
                        if (want != null && a.isPalindrome() != want) return false;
                    }
                    // word_count
                    if (parsedFilters.containsKey("word_count")) {
                        Integer wc = (Integer) parsedFilters.get("word_count");
                        if (wc != null && a.getWordCount() != wc) return false;
                    }
                    // min_length
                    if (parsedFilters.containsKey("min_length")) {
                        Integer min = (Integer) parsedFilters.get("min_length");
                        if (min != null && a.getLength() < min) return false;
                    }
                    // max_length
                    if (parsedFilters.containsKey("max_length")) {
                        Integer max = (Integer) parsedFilters.get("max_length");
                        if (max != null && a.getLength() > max) return false;
                    }
                    // contains_character (single char)
                    if (parsedFilters.containsKey("contains_character")) {
                        String ch = String.valueOf(parsedFilters.get("contains_character"));
                        if (!containsCharacterMatches(a, ch)) return false;
                    }
                    // contains_any_vowel
                    if (parsedFilters.containsKey("contains_any_vowel") && (Boolean) parsedFilters.get("contains_any_vowel")) {
                        if (!valueContainsAnyVowel(a.getValue())) return false;
                    }
                    // starts_with_vowel (first char is a vowel)
                    if (parsedFilters.containsKey("starts_with_vowel") && (Boolean) parsedFilters.get("starts_with_vowel")) {
                        if (!valueStartsWithVowel(a.getValue())) return false;
                    }
                    // startsWith / endsWith (optional strings)
                    if (parsedFilters.containsKey("starts_with")) {
                        String s = String.valueOf(parsedFilters.get("starts_with")).toLowerCase();
                        if (!a.getValue().toLowerCase().startsWith(s)) return false;
                    }
                    if (parsedFilters.containsKey("ends_with")) {
                        String s = String.valueOf(parsedFilters.get("ends_with")).toLowerCase();
                        if (!a.getValue().toLowerCase().endsWith(s)) return false;
                    }

                    // passed all requested checks
                    return true;
                })
                .collect(Collectors.toList());

        return result;
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

    // helpers
    private boolean containsCharacterMatches(AnalyzedString a, String ch) {
        if (ch == null || ch.isEmpty()) return false;
        // check in characterFrequencyMap keys (case-insensitive)
        return a.getCharacterFrequencyMap().keySet().stream()
                .map(String::toLowerCase)
                .anyMatch(k -> k.equals(ch.toLowerCase()));
    }

    private boolean valueContainsAnyVowel(String value) {
        if (value == null) return false;
        return value.toLowerCase().matches(".*[aeiou].*");
    }

    private boolean valueStartsWithVowel(String value) {
        if (value == null || value.isEmpty()) return false;
        char c = Character.toLowerCase(value.charAt(0));
        return "aeiou".indexOf(c) >= 0;
    }
}
