package com.hng.stage1_string_analyzer.service;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NaturalLanguageParserService {

    public Map<String, Object> parse(String query) {
        Map<String, Object> filters = new HashMap<>();
        String lower = query.toLowerCase();

        if (lower.contains("single word")) filters.put("wordCount", 1);
        if (lower.contains("palindromic")) filters.put("isPalindrome", true);

        Matcher m = Pattern.compile("longer than (\\d+)").matcher(lower);
        if (m.find()) filters.put("minLength", Integer.parseInt(m.group(1)) + 1);

        m = Pattern.compile("containing the letter (\\w)").matcher(lower);
        if (m.find()) filters.put("containsCharacter", m.group(1));

        if (filters.isEmpty()) throw new IllegalArgumentException("Unable to parse query");
        return filters;
    }
}
