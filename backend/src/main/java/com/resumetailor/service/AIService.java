package com.resumetailor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.mode:MOCK}")
    private String mode;

    @Value("${hf.api.url:}")
    private String hfUrl;

    @Value("${hf.api.token:}")
    private String hfToken;

    @Value("${hf.chat.url:}")
    private String hfChatUrl;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    void init() {
        log.info("[AIService] mode={}, hfUrl={}, hfChatUrl={}", mode, hfUrl, hfChatUrl);
        if ("HF".equalsIgnoreCase(mode) && (hfUrl == null || hfUrl.isBlank()))
            log.warn("[AIService] HF mode enabled but hf.api.url is blank");
        if ("HF_CHAT".equalsIgnoreCase(mode) && (hfChatUrl == null || hfChatUrl.isBlank()))
            log.warn("[AIService] HF_CHAT mode enabled but hf.chat.url is blank");
    }


    public String generateTailoredResume(String resumeText, String jdText) {
        log.info("[AIService] generateTailoredResume - Resume: {} chars, JD: {} chars",
                resumeText.length(), jdText.length());

        String rawResponse;


        if (resumeText.length() > 3000) {
            rawResponse = generateTailoredResumeEnhanced(resumeText, jdText);
        } else {
            String prompt = buildPrompt(resumeText, jdText);
            rawResponse = callAIService(prompt);
        }


        String cleanedResponse = postProcessAIResponse(rawResponse);

        log.info("[AIService] Response processed - Original: {} chars, Cleaned: {} chars",
                rawResponse.length(), cleanedResponse.length());

        return cleanedResponse;
    }

    public String generateTailoredResumeEnhanced(String resumeText, String jdText) {
        log.info("[AIService] generateTailoredResumeEnhanced - Processing large resume: {} chars", resumeText.length());

        String enhancedPrompt = buildEnhancedPrompt(resumeText, jdText);

        String rawResponse;
        switch (mode.toUpperCase()) {
            case "HF":
                rawResponse = callHuggingFaceEnhanced(enhancedPrompt);
                break;
            case "HF_CHAT":
                rawResponse = callHuggingFaceChatEnhanced(enhancedPrompt);
                break;
            default:
                log.info("[AIService] Using MOCK mode for enhanced tailoring");
                rawResponse = generateMockTailoredResume(resumeText, jdText);
                break;
        }

        return postProcessAIResponse(rawResponse);
    }

    public Map<String, Object> calculateATSScoreWithAI(String resumeContent, String jobDescription) {
        log.info("[AIService] calculateATSScoreWithAI - Resume: {} chars, JD: {} chars",
                resumeContent.length(), jobDescription.length());

        String prompt = buildATSPrompt(resumeContent, jobDescription);

        switch (mode.toUpperCase()) {
            case "HF_CHAT":
                return callATSAnalysis(prompt);
            default:
                log.info("[AIService] Using MOCK mode for ATS analysis");
                return generateMockATSScore(resumeContent, jobDescription);
        }
    }

    private String postProcessAIResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return aiResponse;
        }

        log.debug("[AIService] Post-processing AI response: {} chars", aiResponse.length());

        String processed = aiResponse;

        processed = removeAICommentary(processed);

        processed = cleanFormatting(processed);

        processed = ensureProperStructure(processed);

        log.debug("[AIService] Post-processing complete: {} chars", processed.length());

        return processed.trim();
    }

    private String removeAICommentary(String text) {
        String[] removePatterns = {
                "(?i)\\n\\s*Note:.*$",
                "(?i)\\n\\s*Note -.*$",
                "(?i)\\n\\s*\\*\\*Note\\*\\*:.*$",
                "(?i)\\n\\s*I have rewritten.*$",
                "(?i)\\n\\s*Here is the rewritten.*$",
                "(?i)\\n\\s*This resume has been.*$",
                "(?i)\\n\\s*The above resume.*$",
                "(?i)\\n\\s*Explanation:.*$",
                "(?i)\\n\\s*Analysis:.*$",
                "(?i)\\n\\s*MOCK.*RESUME.*$"
        };

        for (String pattern : removePatterns) {
            text = text.replaceAll(pattern, "");
        }

        return text;
    }

    private String cleanFormatting(String text) {

        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // Bold
        text = text.replaceAll("\\*([^*\n]+)\\*", "$1"); // Italic
        text = text.replaceAll("`([^`]+)`", "$1"); // Code


        text = text.replaceAll("^\\s*[\\*-]\\s+", "• ");
        text = text.replaceAll("\\n\\s*[\\*-]\\s+", "\n• ");


        text = text.replaceAll("^\\s*\\+\\s+", "  ◦ ");
        text = text.replaceAll("\\n\\s*\\+\\s+", "\n  ◦ ");


        text = text.replaceAll("#+\\s*", "");


        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.replaceAll("\\s+$", ""); // Trim trailing spaces

        return text;
    }

    private String ensureProperStructure(String text) {
        // Ensure sections are properly spaced
        String[] commonSections = {
                "SUMMARY", "OBJECTIVE", "EXPERIENCE", "PROFESSIONAL EXPERIENCE",
                "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS"
        };

        for (String section : commonSections) {
            // Add proper spacing before sections if missing
            String pattern = "(?<!\\n\\n)" + section;
            text = text.replaceAll(pattern, "\n\n" + section);
        }

        // Clean up any excessive spacing created
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.replaceAll("^\\n+", ""); // Remove leading newlines

        return text;
    }

    private String callAIService(String prompt) {
        switch (mode.toUpperCase()) {
            case "HF":
                return callHuggingFace(prompt);
            case "HF_CHAT":
                return callHuggingFaceChat(prompt);
            default:
                log.info("[AIService] Using MOCK mode, not calling external API");
                return "MOCK TAILORED RESUME:\nBased on the job description, your resume has been rewritten.";
        }
    }

    private String callHuggingFaceChatEnhanced(String prompt) {
        if (hfChatUrl == null || hfChatUrl.isBlank() || hfToken == null || hfToken.isBlank()) {
            log.error("[AIService] Missing HF Chat config for enhanced processing");
            return "HF Chat configuration missing. Set hf.chat.url & hf.api.token.";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Increase max_tokens for large resume processing
            String requestBody = String.format("""
                {
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "model": "meta-llama/Llama-3.1-8B-Instruct",
                  "max_tokens": 2000,
                  "temperature": 0.3
                }
                """, jsonEscape(prompt));

            log.info("[AIService] Enhanced POST to HF Chat {}", hfChatUrl);
            ResponseEntity<String> resp = rest.exchange(hfChatUrl, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);

            String raw = resp.getBody();
            log.info("[AIService] Enhanced HF Chat status={} response_len={}",
                    resp.getStatusCodeValue(), raw == null ? 0 : raw.length());

            if (raw == null || raw.isBlank()) return "Empty response from HF Chat.";

            JsonNode json = mapper.readTree(raw);
            if (json.has("choices") && json.get("choices").isArray() && json.get("choices").size() > 0) {
                JsonNode firstChoice = json.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    String content = firstChoice.get("message").get("content").asText();
                    log.info("[AIService] Enhanced tailoring result length: {}", content.length());
                    return content;
                }
            }

            log.warn("[AIService] Unexpected enhanced HF Chat response format");
            return raw;
        } catch (Exception ex) {
            log.error("[AIService] Enhanced HF Chat call failed: {}", ex.toString());
            return "Enhanced HF Chat API error: " + ex.getMessage();
        }
    }

    private String callHuggingFaceEnhanced(String prompt) {
        // For the old HF API, we still need to limit prompt size
        String limitedPrompt = prompt.length() > 1200 ? prompt.substring(0, 1200) + "..." : prompt;
        return callHuggingFace(limitedPrompt);
    }

    private Map<String, Object> callATSAnalysis(String prompt) {
        if (hfChatUrl == null || hfChatUrl.isBlank() || hfToken == null || hfToken.isBlank()) {
            log.error("[AIService] Missing HF Chat config for ATS analysis");
            return generateMockATSScore("", "");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format("""
                {
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "model": "meta-llama/Llama-3.1-8B-Instruct",
                  "max_tokens": 1000,
                  "temperature": 0.1
                }
                """, jsonEscape(prompt));

            ResponseEntity<String> resp = rest.exchange(hfChatUrl, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);

            String raw = resp.getBody();
            if (raw == null || raw.isBlank()) {
                return generateMockATSScore("", "");
            }

            JsonNode json = mapper.readTree(raw);
            if (json.has("choices") && json.get("choices").isArray() && json.get("choices").size() > 0) {
                JsonNode firstChoice = json.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    String content = firstChoice.get("message").get("content").asText();
                    return parseATSResponse(content);
                }
            }

            return generateMockATSScore("", "");
        } catch (Exception ex) {
            log.error("[AIService] ATS analysis call failed: {}", ex.toString());
            return generateMockATSScore("", "");
        }
    }


    private String callHuggingFaceChat(String prompt) {
        if (hfChatUrl == null || hfChatUrl.isBlank() || hfToken == null || hfToken.isBlank()) {
            log.error("[AIService] Missing HF Chat config. hfChatUrl='{}' tokenPresent={}", hfChatUrl, hfToken != null && !hfToken.isBlank());
            return "HF Chat configuration missing. Set hf.chat.url & hf.api.token.";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format("""
                {
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "model": "meta-llama/Llama-3.1-8B-Instruct",
                  "max_tokens": 1000,
                  "temperature": 0.7
                }
                """, jsonEscape(prompt));

            log.info("[AIService] POST to HF Chat {}", hfChatUrl);
            ResponseEntity<String> resp = rest.exchange(hfChatUrl, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);

            String raw = resp.getBody();
            log.info("[AIService] HF Chat status={} len={}", resp.getStatusCodeValue(), raw == null ? 0 : raw.length());
            if (raw == null || raw.isBlank()) return "Empty response from HF Chat.";

            JsonNode json = mapper.readTree(raw);
            if (json.has("choices") && json.get("choices").isArray() && json.get("choices").size() > 0) {
                JsonNode firstChoice = json.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    return firstChoice.get("message").get("content").asText();
                }
            }

            log.warn("[AIService] Unexpected HF Chat response format: {}", raw);
            return raw;
        } catch (Exception ex) {
            log.error("[AIService] HF Chat call failed: {}", ex.toString());
            return "HF Chat API error: " + ex.getMessage();
        }
    }

    private String callHuggingFace(String prompt) {
        if (hfUrl == null || hfUrl.isBlank() || hfToken == null || hfToken.isBlank()) {
            log.error("[AIService] Missing HF config. hfUrl='{}' tokenPresent={}", hfUrl, hfToken != null && !hfToken.isBlank());
            return "HF configuration missing. Set hf.api.url & hf.api.token.";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String shortPrompt = prompt.length() > 800 ? prompt.substring(0, 800) : prompt;
            String body = "{\"inputs\":" + jsonEscape(shortPrompt) + "}";

            log.info("[AIService] POST to HF {}", hfUrl);
            ResponseEntity<String> resp = rest.exchange(hfUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            String raw = resp.getBody();
            log.info("[AIService] HF status={} len={}", resp.getStatusCodeValue(), raw == null ? 0 : raw.length());
            if (raw == null || raw.isBlank()) return "Empty response from HF.";

            JsonNode arr = mapper.readTree(raw);
            if (arr.isArray() && arr.size() > 0) {
                JsonNode first = arr.get(0);
                if (first.has("generated_text")) {
                    return first.get("generated_text").asText();
                } else if (first.has("summary_text")) {
                    return first.get("summary_text").asText();
                }
            }
            return raw;
        } catch (Exception ex) {
            log.error("[AIService] HF call failed: {}", ex.toString());
            return "HF API error: " + ex.getMessage();
        }
    }


    private String buildEnhancedPrompt(String resume, String jd) {
        return String.format("""
            You are an expert resume writer. Your task is to completely rewrite this resume to match the job description.
            
            CRITICAL INSTRUCTIONS:
            1. Process the ENTIRE resume - do not truncate or summarize
            2. Rewrite ALL sections to align with job requirements
            3. Keep the same professional structure and format
            4. Include ALL work experiences but emphasize relevant ones
            5. Match keywords from job description naturally
            6. Return ONLY the complete tailored resume text
            7. DO NOT add any notes, explanations, or commentary at the end
            8. DO NOT use markdown formatting (**, *, etc.) - use plain text
            9. Use bullet points (•) for lists, not asterisks
            
            JOB DESCRIPTION:
            %s
            
            ORIGINAL RESUME TO REWRITE:
            %s
            
            TAILORED RESUME:""",
                safeTruncate(jd, 2000), safeTruncate(resume, 4000));
    }

    private String buildATSPrompt(String resume, String jd) {
        return String.format("""
            You are an ATS (Applicant Tracking System) analyzer. Analyze this resume against the job description.
            
            Job Description:
            %s
            
            Resume:
            %s
            
            Provide analysis in this exact format:
            SCORE: [number 0-100]
            MATCHING_KEYWORDS: keyword1, keyword2, keyword3
            MISSING_KEYWORDS: missing1, missing2, missing3
            SUGGESTIONS: suggestion1 | suggestion2 | suggestion3
            
            Analysis:""",
                safeTruncate(jd, 1500), safeTruncate(resume, 2000));
    }

    private String buildPrompt(String resume, String jd) {
        return String.format("""
            You are an expert resume writer. Rewrite the resume to fit the job description. Keep the same structure but tailor the content.
            
            IMPORTANT RULES:
            1. Return ONLY the tailored resume text
            2. NO explanatory notes or commentary
            3. NO markdown formatting - use plain text only
            4. Use bullet points (•) not asterisks (*)
            5. Match keywords from job description naturally
            
            === JOB DESCRIPTION ===
            %s
            
            === ORIGINAL RESUME ===
            %s
            
            TAILORED RESUME:""", safe(jd), safe(resume));
    }


    private String generateMockTailoredResume(String resume, String jd) {
        return String.format("""
            John Doe | Software Developer
            Email: john.doe@email.com | Phone: (555) 123-4567
            
            SUMMARY
            Experienced software developer with %d years of experience in full-stack development. 
            Skilled in Java, Spring Boot, and modern web technologies. Proven track record of 
            delivering high-quality applications that meet business requirements.
            
            PROFESSIONAL EXPERIENCE
            
            Software Developer | Tech Company | 2020 - Present
            • Developed and maintained web applications using Java and Spring Boot
            • Collaborated with cross-functional teams to deliver projects on time
            • Implemented RESTful APIs and microservices architecture
            • Optimized application performance and reduced load times by 30%%
            
            Junior Developer | Previous Company | 2018 - 2020  
            • Built responsive web interfaces using HTML, CSS, and JavaScript
            • Participated in code reviews and maintained coding standards
            • Worked with databases to design and optimize queries
            
            EDUCATION
            Bachelor of Science in Computer Science | University Name | 2018
            
            SKILLS
            • Programming: Java, Python, JavaScript, SQL
            • Frameworks: Spring Boot, React, Node.js
            • Databases: MySQL, PostgreSQL, MongoDB
            • Tools: Git, Docker, Jenkins, AWS
            """, Math.min(resume.length() / 500, 5));
    }

    private Map<String, Object> generateMockATSScore(String resume, String jd) {
        Map<String, Object> result = new HashMap<>();
        result.put("score", 78);
        result.put("breakdown", Map.of(
                "keywordMatch", 75,
                "skillsMatch", 82,
                "experienceMatch", 76,
                "formatMatch", 80
        ));
        result.put("matchingKeywords", Arrays.asList("Java", "Spring Boot", "REST API", "SQL"));
        result.put("missingKeywords", Arrays.asList("Docker", "Kubernetes", "AWS"));
        result.put("suggestions", Arrays.asList(
                "Add Docker containerization experience",
                "Include cloud platform experience",
                "Highlight microservices architecture experience"
        ));
        return result;
    }


    private Map<String, Object> parseATSResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Parse the structured response
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("SCORE:")) {
                    String scoreStr = line.substring(6).trim();
                    result.put("score", Integer.parseInt(scoreStr.replaceAll("[^0-9]", "")));
                } else if (line.startsWith("MATCHING_KEYWORDS:")) {
                    String keywords = line.substring(18).trim();
                    result.put("matchingKeywords", Arrays.asList(keywords.split(",\\s*")));
                } else if (line.startsWith("MISSING_KEYWORDS:")) {
                    String keywords = line.substring(17).trim();
                    result.put("missingKeywords", Arrays.asList(keywords.split(",\\s*")));
                } else if (line.startsWith("SUGGESTIONS:")) {
                    String suggestions = line.substring(12).trim();
                    result.put("suggestions", Arrays.asList(suggestions.split("\\|\\s*")));
                }
            }


            result.put("breakdown", Map.of(
                    "keywordMatch", (Integer) result.getOrDefault("score", 0),
                    "overall", (Integer) result.getOrDefault("score", 0)
            ));

        } catch (Exception e) {
            log.error("[AIService] Error parsing ATS response: {}", e.getMessage());
            return generateMockATSScore("", "");
        }
        return result;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.length() > 3000 ? s.substring(0, 3000) : s;
    }

    private static String safeTruncate(String s, int maxLength) {
        if (s == null) return "";
        return s.length() > maxLength ? s.substring(0, maxLength) + "..." : s;
    }

    private static String jsonEscape(String s) {
        if (s == null) return "\"\"";
        String esc = s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
        return "\"" + esc + "\"";
    }
}