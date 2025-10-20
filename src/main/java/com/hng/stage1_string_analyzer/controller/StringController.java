package com.hng.stage1_string_analyzer.controller;

import com.hng.stage1_string_analyzer.model.AnalyzedString;
import com.hng.stage1_string_analyzer.service.NaturalLanguageParserService;
import com.hng.stage1_string_analyzer.service.StringAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/strings")
public class StringController {

    private final StringAnalysisService analysisService;
    private final NaturalLanguageParserService nlService;

    public StringController(StringAnalysisService analysisService, NaturalLanguageParserService nlService) {
        this.analysisService = analysisService;
        this.nlService = nlService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Object valueObj = body.get("value");
        if (valueObj == null) return ResponseEntity.badRequest().body(Map.of("error", "Missing 'value' field"));
        if (!(valueObj instanceof String)) return ResponseEntity.unprocessableEntity().body(Map.of("error", "Value must be a string"));

        String value = (String) valueObj;
        try {
            AnalyzedString analyzed = analysisService.analyzeAndSave(value);
            return ResponseEntity.status(HttpStatus.CREATED).body(analyzed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{stringValue}")
    public ResponseEntity<?> get(@PathVariable String stringValue) {
        return analysisService.getByValue(stringValue)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "String not found")));
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Boolean is_palindrome,
            @RequestParam(required = false) Integer min_length,
            @RequestParam(required = false) Integer max_length,
            @RequestParam(required = false) Integer word_count,
            @RequestParam(required = false) String contains_character
    ) {
        var result = analysisService.filter(is_palindrome, min_length, max_length, word_count, contains_character);
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("count", result.size());
        response.put("filters_applied", Map.of(
                "is_palindrome", is_palindrome,
                "min_length", min_length,
                "max_length", max_length,
                "word_count", word_count,
                "contains_character", contains_character
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter-by-natural-language")
    public ResponseEntity<?> naturalLanguage(@RequestParam String query) {
        try {
            var parsed = nlService.parse(query);
            var result = analysisService.filter(
                    (Boolean) parsed.get("isPalindrome"),
                    (Integer) parsed.get("minLength"),
                    null,
                    (Integer) parsed.get("wordCount"),
                    (String) parsed.get("containsCharacter")
            );
            return ResponseEntity.ok(Map.of(
                    "data", result,
                    "count", result.size(),
                    "interpreted_query", Map.of(
                            "original", query,
                            "parsed_filters", parsed
                    )
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{stringValue}")
    public ResponseEntity<?> delete(@PathVariable String stringValue) {
        try {
            analysisService.deleteByValue(stringValue);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
