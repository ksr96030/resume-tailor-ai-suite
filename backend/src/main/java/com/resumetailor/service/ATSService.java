package com.resumetailor.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ATSService {

    private static final Set<String> STOPWORDS = Set.of(
            "the","a","an","and","or","but","to","in","on","for","of","with","by","at",
            "from","as","is","are","was","were","be","been","being","this","that","these","those",
            "it","its","you","your","we","our","they","their"
    );

    private static final Pattern TOKENIZER = Pattern.compile("[^a-z0-9+.#]+"); // keep tech-ish tokens like c++, c#, .net

    private static List<String> tokenize(String text) {
        return Arrays.stream(TOKENIZER.split(text.toLowerCase()))
                .filter(t -> t.length() > 1)
                .collect(Collectors.toList());
    }

    public int calculateATSScore(String resume, String jd) {
        var resumeTokens = new HashSet<>(tokenize(resume));
        // remove stopwords from resume for fairness
        resumeTokens.removeAll(STOPWORDS);

        // JD tokens (unique, stopwords removed)
        var jdTokens = new LinkedHashSet<>(tokenize(jd));
        jdTokens.removeAll(STOPWORDS);

        if (jdTokens.isEmpty()) return 0;

        int matches = 0;
        for (String token : jdTokens) {
            if (resumeTokens.contains(token)) {
                matches++;
            }
        }
        int score = (int)Math.round((matches * 100.0) / jdTokens.size());
        return Math.max(0, Math.min(100, score));
    }
}
