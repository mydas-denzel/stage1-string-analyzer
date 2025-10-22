package com.hng.stage1_string_analyzer.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NaturalLanguageParserService {

    public Map<String, Object> parse(String query) {
        Map<String, Object> filters = new HashMap<>();
        List<String> containsCharacters = new ArrayList<>();

        String lower = query.toLowerCase().trim();

        // --- Word count filters ---
        if (lower.contains("single word") || lower.contains("one word"))
            filters.put("word_count", 1);
        if (lower.contains("two words") || lower.contains("double word"))
            filters.put("word_count", 2);
        if (lower.contains("multi word") || lower.contains("multiple words"))
            filters.put("word_count", 2); // heuristic

        // --- Palindrome filters ---
        if (lower.contains("palindromic") || lower.contains("palindrome"))
            filters.put("is_palindrome", true);

        // --- Length filters ---
        Matcher m = Pattern.compile("longer than (\\d+)").matcher(lower);
        if (m.find()) filters.put("min_length", Integer.parseInt(m.group(1)) + 1);

        m = Pattern.compile("shorter than (\\d+)").matcher(lower);
        if (m.find()) filters.put("max_length", Integer.parseInt(m.group(1)) - 1);

        // --- Specific letter filters ---
        Matcher letterMatcher = Pattern.compile("containing the letter (\\w)").matcher(lower);
        while (letterMatcher.find()) {
            containsCharacters.add(letterMatcher.group(1));
        }

        // --- Vowel detection (general) ---
        if (lower.contains("containing a vowel") ||
                lower.contains("containing any vowel") ||
                lower.contains("has a vowel") ||
                lower.contains("has any vowel") ||
                lower.contains("contains vowel") ||
                lower.contains("vowels")) {
            filters.put("contains_any_vowel", true);
        }

        // --- Specific vowel positions ---
        Map<String, String> vowelPositions = Map.of(
                "first vowel", "a",
                "second vowel", "e",
                "third vowel", "i",
                "fourth vowel", "o",
                "fifth vowel", "u"
        );

        for (Map.Entry<String, String> entry : vowelPositions.entrySet()) {
            if (lower.contains(entry.getKey())) {
                containsCharacters.add(entry.getValue());
            }
        }

        // --- Direct vowel mentions (explicit) ---
        for (char vowel : new char[]{'a', 'e', 'i', 'o', 'u'}) {
            if (lower.contains("containing the letter " + vowel))
                containsCharacters.add(String.valueOf(vowel));
        }

        // --- Deduplicate characters ---
        if (!containsCharacters.isEmpty()) {
            // If only one character, use single; otherwise, use list
            if (containsCharacters.size() == 1)
                filters.put("contains_character", containsCharacters.get(0));
            else
                filters.put("contains_characters", new HashSet<>(containsCharacters)); // multiple
        }

        // --- Conflict check ---
        if (filters.containsKey("min_length") && filters.containsKey("max_length")) {
            int min = (int) filters.get("min_length");
            int max = (int) filters.get("max_length");
            if (min > max) {
                throw new IllegalStateException("Conflicting filters: min_length > max_length");
            }
        }

        // --- If nothing matched ---
        if (filters.isEmpty())
            throw new IllegalArgumentException("Unable to parse natural language query");

        return filters;
    }
}
