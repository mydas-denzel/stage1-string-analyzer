package com.hng.stage1_string_analyzer.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NaturalLanguageParserService {

    /**
     * Parses a natural language query and returns a map of snake_case filter keys.
     * Examples supported:
     *  - "all single word palindromic strings" -> { "word_count": 1, "is_palindrome": true }
     *  - "strings longer than 10 characters" -> { "min_length": 11 }
     *  - "palindromic strings that contain the first vowel" -> { "is_palindrome": true, "starts_with_vowel": true }
     *  - "strings containing the letter z" -> { "contains_character": "z" }
     *  - "strings containing a vowel" -> { "contains_any_vowel": true }
     *
     * Throws IllegalArgumentException when query cannot be parsed.
     * Throws IllegalStateException when parsed filters are conflicting (e.g. min_length > max_length).
     */
    public Map<String, Object> parse(String query) {
        if (query == null) throw new IllegalArgumentException("Unable to parse natural language query");

        Map<String, Object> filters = new HashMap<>();
        String lower = query.toLowerCase().trim();

        // word count heuristics
        if (lower.contains("single word") || lower.contains("one word")) {
            filters.put("word_count", 1);
        } else if (lower.contains("two words") || lower.contains("two word")) {
            filters.put("word_count", 2);
        } else if (lower.contains("multi word") || lower.contains("multiple words") || lower.contains("multi-word")) {
            filters.put("word_count", 2); // heuristic
        }

        // palindromic
        if (lower.contains("palindromic") || lower.contains("palindrome")) {
            filters.put("is_palindrome", true);
        }

        // longer than X -> min_length = X+1 (to match wording semantics)
        Matcher m = Pattern.compile("longer than (\\d+)").matcher(lower);
        if (m.find()) {
            filters.put("min_length", Integer.parseInt(m.group(1)) + 1);
        }

        // shorter than X -> max_length = X-1
        m = Pattern.compile("shorter than (\\d+)").matcher(lower);
        if (m.find()) {
            filters.put("max_length", Integer.parseInt(m.group(1)) - 1);
        }

        // containing the letter X  (e.g. "containing the letter z", "containing z")
        m = Pattern.compile("containing(?: the letter)? (\\w)").matcher(lower);
        if (m.find()) {
            filters.put("contains_character", m.group(1));
        }

        // direct vowel mentions: "containing a vowel", "contains vowel", "has vowel", etc.
        if (lower.contains("containing a vowel") ||
                lower.contains("containing any vowel") ||
                lower.contains("has a vowel") ||
                lower.contains("has any vowel") ||
                lower.contains("contains vowel") ||
                lower.contains("containing vowels") ||
                lower.contains("contains vowels") ||
                lower.contains("has vowels") ) {
            filters.put("contains_any_vowel", true);
        }

        // first vowel / starts with a vowel -> means first character is vowel
        if (lower.contains("first vowel") || lower.contains("starts with a vowel") || lower.contains("starting with a vowel")) {
            filters.put("starts_with_vowel", true);
        }

        // optionally: if query explicitly mentions a vowel letter (e.g. "letter a")
        m = Pattern.compile("containing the letter (a|e|i|o|u)").matcher(lower);
        if (m.find()) {
            filters.put("contains_character", m.group(1));
        }

        // conflict detection for min/max length
        if (filters.containsKey("min_length") && filters.containsKey("max_length")) {
            int min = (int) filters.get("min_length");
            int max = (int) filters.get("max_length");
            if (min > max) {
                throw new IllegalStateException("Conflicting filters: min_length > max_length");
            }
        }

        if (filters.isEmpty()) {
            throw new IllegalArgumentException("Unable to parse natural language query");
        }

        return filters;
    }
}
