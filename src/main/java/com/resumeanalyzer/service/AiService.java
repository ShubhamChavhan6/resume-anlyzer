package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.model.AnalysisResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${GEMINI_API_KEY:demo-key}")
    private String apiKey;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String cachedModelUrl;

    public AiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public AnalysisResponse analyzeResume(String resumeText, String jobRole, String jobDescription,
            String reviewerType) {
        if ("demo-key".equals(apiKey)) {
            // Mock response for development
            return new AnalysisResponse(
                    85,
                    90, // skills
                    80, // experience
                    75, // formatting
                    80, // clarity
                    "A strong candidate with solid experience in Java and Cloud technologies, but lacks quantitative impact in project descriptions.",
                    List.of("Add clear experience or projects relevant to Java Backend roles.",
                            "Rewrite bullets to show impact, tools, and results.",
                            "Add a focused summary aligned with the job."),
                    List.of("Strong Spring Boot knowledge", "Cloud-native architecture experience",
                            "Clear education history"),
                    List.of("[Must-have] Spring Security", "[Nice-to-have] Docker", "[Must-have] Kubernetes"),
                    List.of("Phone number", "LinkedIn link and GitHub profile"),
                    List.of("Before: 'Implemented API' -> After: 'Designed RESTful API handling 10k+ requests/day, reducing latency by 40%'",
                            "Action: Add a 'Skills' section at the top"),
                    List.of(
                            new AnalysisResponse.SectionFeedback("Experience", List.of("Strong technical depth"),
                                    List.of("Needs more quantifiable metrics like 'increased efficiency by 20%'")),
                            new AnalysisResponse.SectionFeedback("Projects", List.of("Relevant domain projects"),
                                    List.of("Missing live links or GitHub repos"))),
                    List.of("Flagged buzzwords: 'Hard-working' -> Suggest using 'Dedicated' or removing entirely"));
        }

        String prompt = buildAnalysisPrompt(resumeText, jobRole, jobDescription, reviewerType);

        try {
            // 0. Resolve Model URL dynamically if not already cached
            if (cachedModelUrl == null) {
                cachedModelUrl = resolveBestAvailableModel();
            }

            // 1. Construct Request Body
            // 1. Construct Request Body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 2. Call API
            String url = cachedModelUrl + "?key=" + apiKey;
            String rawResponse = restTemplate.postForObject(url, entity, String.class);

            // 3. Extract and Clean JSON
            String jsonContent = extractJsonString(rawResponse);

            // 4. Sanitize and Map to DTO
            JsonNode responseRoot = objectMapper.readTree(jsonContent);

            // Fix: Handle "None" strings in list fields
            sanitizeListField(responseRoot, "grammarIssues");
            sanitizeListField(responseRoot, "missingSkills");
            sanitizeListField(responseRoot, "missingContactInfo");
            sanitizeListField(responseRoot, "strengths");
            sanitizeListField(responseRoot, "actionableEdits");
            if (responseRoot.has("sectionFeedback") && !responseRoot.get("sectionFeedback").isArray()) {
                if (responseRoot instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) responseRoot).putArray("sectionFeedback");
                }
            }
            sanitizeListField(responseRoot, "topFixes");

            AnalysisResponse tempResponse = objectMapper.treeToValue(responseRoot, AnalysisResponse.class);
            if (tempResponse.missingSkills() != null && tempResponse.missingSkills().size() > 5) {
                return new AnalysisResponse(
                        tempResponse.score(), tempResponse.skillsScore(), tempResponse.experienceScore(),
                        tempResponse.formattingScore(), tempResponse.clarityScore(),
                        tempResponse.summary(), tempResponse.topFixes(), tempResponse.strengths(),
                        tempResponse.missingSkills().stream().limit(5).toList(),
                        tempResponse.missingContactInfo(),
                        tempResponse.actionableEdits(), tempResponse.sectionFeedback(),
                        tempResponse.grammarIssues());
            }
            return tempResponse;

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                errorMsg += " | Response: "
                        + ((org.springframework.web.client.HttpStatusCodeException) e).getResponseBodyAsString();
            }
            System.err.println("AI Service Error: " + errorMsg);
            try {
                java.nio.file.Files.writeString(java.nio.file.Paths.get("last_ai_error.txt"), errorMsg);
            } catch (Exception ignored) {
            }

            cachedModelUrl = null; // Invalidate cache on error to retry discovery next time
            throw new RuntimeException("AI Analysis failed: " + errorMsg);
        }
    }

    public String extractTextFromImage(byte[] imageBytes, String mimeType) {
        if ("demo-key".equals(apiKey)) {
            return "Demo OCR Text Result:\nJohn Doe\nSoftware Engineer\nUsed Java, Spring Boot, created REST APIs.";
        }
        try {
            if (cachedModelUrl == null) {
                cachedModelUrl = resolveBestAvailableModel();
            }
            String encodedImage = java.util.Base64.getEncoder().encodeToString(imageBytes);
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text",
                                            "You are a precise document OCR engine. Extract all text from this resume image exactly as it is written. Only output the extracted text, do not add any commentary."),
                                    Map.of("inlineData", Map.of(
                                            "mimeType", mimeType,
                                            "data", encodedImage))))),
                    "generationConfig", Map.of(
                            "response_mime_type", "text/plain"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = cachedModelUrl + "?key=" + apiKey;
            String rawResponse = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to OCR image via AI: " + e.getMessage());
        }
    }

    /**
     * Dynamically discovers and selects the best available Gemini model for the
     * provided API key.
     * <p>
     * This method:
     * 1. Calls the Google AI API to list all models available to the key.
     * 2. Filters for 'Gemini' models.
     * 3. Prioritizes 'Flash' models (faster, cheaper) over 'Pro' models.
     * 4. Returns the full URL for the selected model.
     * </p>
     * 
     * @return The API URL for the best available model.
     */
    private String resolveBestAvailableModel() {
        try {
            // Step 1: List all models available to this API Key
            String listModelsUrl = BASE_URL + "/models?key=" + apiKey;
            String response = restTemplate.getForObject(listModelsUrl, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode modelsNode = root.path("models");

            List<String> availableModels = new ArrayList<>();
            if (modelsNode.isArray()) {
                for (JsonNode model : modelsNode) {
                    availableModels.add(model.path("name").asText());
                }
            }

            // Step 2: Select the best model based on priority
            // Priority 1: "1.5-Flash" models to avoid 503 high demand errors on newest models.
            String selectedModel = availableModels.stream()
                    .filter(name -> name.contains("gemini-1.5-flash") && !name.contains("8b"))
                    .findFirst()
                    .orElse(null);

            // Priority 2: Any "Flash" model
            if (selectedModel == null) {
                selectedModel = availableModels.stream()
                        .filter(name -> name.contains("gemini") && name.contains("flash") && !name.contains("8b"))
                        .findFirst()
                        .orElse(null);
            }

            // Priority 3: "Pro" models (Higher reasoning, slower/more expensive)
            if (selectedModel == null) {
                selectedModel = availableModels.stream()
                        .filter(name -> name.contains("gemini") && name.contains("pro"))
                        .findFirst()
                        .orElse(null);
            }

            // Priority 3: Fallback to ANY model with "gemini" in the name
            if (selectedModel == null) {
                selectedModel = availableModels.stream()
                        .filter(name -> name.contains("gemini"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No Gemini models found for this API Key"));
            }

            // Step 3: Construct the final API URL
            // Format: .../models/{modelName}:generateContent
            return BASE_URL + "/" + selectedModel + ":generateContent";

        } catch (Exception e) {
            throw new RuntimeException("Failed to list/select AI models: " + e.getMessage());
        }
    }

    private String extractJsonString(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        // 1. Remove markdown code blocks if present
        if (aiText.contains("```json")) {
            aiText = aiText.substring(aiText.indexOf("```json") + 7);
            if (aiText.contains("```")) {
                aiText = aiText.substring(0, aiText.lastIndexOf("```"));
            }
        } else if (aiText.contains("```")) {
            aiText = aiText.substring(aiText.indexOf("```") + 3);
            if (aiText.contains("```")) {
                aiText = aiText.substring(0, aiText.lastIndexOf("```"));
            }
        }

        aiText = aiText.trim();

        // 2. Sanitize: Escape specific control characters if they are inside the string
        // but not escaped.
        // This is a naive heuristic but handles the most common Gemini issue: literal
        // newlines in JSON strings.
        // NOTE: A proper specific JSON sanitizer library would be better, but we don't
        // have one dependency-wise.
        // We will try to rely on the prompt first, but here we can at least log it.
        // Re-enforcing the prompt in the other method is the primary fix.

        return aiText;
    }

    private String buildAnalysisPrompt(String resumeText, String jobRole, String jobDescription, String reviewerType) {
        String perspectiveInstruction = reviewerType.equalsIgnoreCase("HR Recruiter")
                ? "Adopt the persona of a sharp HR Recruiter. Focus on clarity, soft skills, culture fit, and formatting. Value concise communication."
                : "Adopt the persona of a Senior Interviewer (Domain Expert). Focus on relevant domain skills, depth of experience, and concrete impact. Value measurable results.";

        String comparisonContext = (jobDescription != null && !jobDescription.isBlank())
                ? "Use the provided JOB DESCRIPTION TEXT to identify exact keyword matches and missing skills."
                : "Use standard industry requirements for the role of '" + jobRole + "' to identify gaps.";

        return """
                ### ROLE
                You are an ATS and recruiter specifically for the role: "%s" reviewing this resume.
                Perspective: %s.
                Only evaluate and suggest improvements relevant to "%s".
                Do NOT assume Java/Cloud/IT unless they are actually present in the resume AND highly relevant to "%s".

                ### TASK
                1. Analyze the RESUME TEXT strictly against the target JOB ROLE.
                2. %s
                3. Identify specific strengths and critical missing skills (Must-Have vs Nice-to-Have) for this exact role.
                4. Provide concrete, actionable rewrite suggestions, including a "Before -> After" format for weak bullets, showing quantified impact relevant to the role. Use industry-appropriate examples based on the target role (e.g. for a Teacher, use education metrics; for Sales, use revenue metrics).
                5. Add a "buzzword / fluff detector" that flags vague phrases (like "hard-working, passionate") and suggests stronger alternatives.
                6. Check for basic Contact Information (Name, Email, Phone, Location, LinkedIn). If the Target Role is technical (e.g., developer, engineer), explicitly look for a GitHub/Portfolio link.
                7. Provide a quantitative fit score (0-100) and sub-scores.

                ### STRICT CONSTRAINTS & IT BIAS GUARD
                - **Role Consistency Guard**: If the resume content reflects a completely different industry (e.g., mostly IT/Java) but the target role is non-IT (e.g., Primary Teacher, Sales Executive), DO NOT twist the feedback to pretend they are applying for an IT role. Instead, explicitly warn the user that their current resume is severely misaligned for the target role ("%s"). Refuse to act like they are applying for a Java developer if they selected Primary Teacher.
                - **Tone & Format**: Use neutral, recruiter-style language. Standardize bullets to read like real HR feedback.
                - **Top Fixes**: Provide exactly three short, highly actionable bullets ('Top 3 things to fix first') a jobseeker can do in the next 7 days.
                - **Strengths**: Exactly 3 distinct bullets, 1 short line each.
                - **Missing Skills**: Group missing or weak keywords as '[Must-have]' or '[Nice-to-have]'. Max 3-5 keywords relevant to "%s".
                - **Actionable Edits**: Start with ONE improved professional summary tailored to "%s". Suggest 3-5 "Before: ... -> After: ..." rewrites for bullet points.
                - **JSON Strictness**: DO NOT output any markdown tags like ```json. ONLY output the raw JSON object.

                --------------------------------------------------------
                ::: JSON OUTPUT REQUIREMENT :::
                You MUST return the result as a valid JSON object.
                Format:
                {
                  "score": (Integer 0-100),
                  "skillsScore": (Integer 0-100),
                  "experienceScore": (Integer 0-100),
                  "formattingScore": (Integer 0-100),
                  "clarityScore": (Integer 0-100),
                  "summary": "Professional summary (max 3 sentences). Always describe fit for the specific target role.",
                  "topFixes": ["Top actionable bullet 1", "Top actionable bullet 2", "Top actionable bullet 3"],
                  "strengths": ["Strength 1", "Strength 2", "Strength 3"],
                  "missingSkills": ["[Must-have] Keyword 1", "[Nice-to-have] Keyword 2"],
                  "missingContactInfo": ["Missing item 1", "Missing item 2"],
                  "actionableEdits": ["Before: [weak] -> After: [quantified for role]"],
                  "sectionFeedback": [
                      {
                        "sectionName": "Summary",
                        "good": ["what's good"],
                        "improvements": ["what to improve"]
                      }
                  ],
                  "grammarIssues": ["grammar/fluff issues or 'None'"]
                }

                ### INPUT DATA
                TARGET JOB ROLE: %s
                JOB DESCRIPTION (Priority):
                %s

                RESUME TEXT:
                %s
                """
                .formatted(
                        jobRole, perspectiveInstruction, jobRole, jobRole,
                        comparisonContext,
                        jobRole,
                        jobRole, jobRole,
                        jobRole,
                        (jobDescription != null && !jobDescription.isBlank() ? jobDescription : "Not Provided"),
                        resumeText
                );
    }

    private void sanitizeListField(JsonNode root, String fieldName) {
        if (root.has(fieldName) && !root.get(fieldName).isArray()) {
            // If it's not an array (e.g. "None", "N/A", null), replace with empty array
            if (root instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) root).putArray(fieldName);
            }
        }
    }
}
