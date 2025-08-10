package com.resumetailor.controller;

import com.resumetailor.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIDebugController {
    private final AIService ai;
    public AIDebugController(AIService ai){ this.ai = ai; }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        String out = ai.generateTailoredResume(
                "Senior Java dev with Spring & MySQL. 5+ yrs. Built REST APIs.",
                "Hiring Java developer with Spring Boot, REST, MySQL. React is a plus."
        );
        return ResponseEntity.ok(out);
    }
}