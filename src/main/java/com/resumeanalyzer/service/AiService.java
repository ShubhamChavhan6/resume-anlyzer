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
                ? "Adopt the persona of a sharp HR Recruiter. Focus on clarity, formatting, soft skills, culture fit, and red flags. Value concise communication and layout."
                : "Adopt the persona of a Senior Interviewer (Domain Expert). Focus on relevant domain skills, depth of experience, core competencies, and concrete impact. Value measurable results and accuracy.";

        String comparisonContext = (jobDescription != null && !jobDescription.isBlank())
                ? "Use the provided JOB DESCRIPTION TEXT to identify exact keyword matches and missing skills."
                : "Use standard industry requirements for the role of '" + jobRole + "' to identify gaps.";

        return """
                ### ROLE
                You are an Automated Resume Screening System.
                %s
                Your task is to review this resume for the role of: "%s".

                ### TASK
                1. Analyze the RESUME TEXT against the JOB ROLE.
                2. %s
                3. Identify specific strengths and critical missing skills (Must-Have vs Nice-to-Have).
                4. Provide concrete, actionable rewrite suggestions, including a "Before -> After" format for weak bullets, showing quantified impact. Where appropriate, use examples relevant to the Indian market (e.g. "Java Developer at TCS", "Sales Executive in FMCG - Mumbai").
                5. Add a "buzzword / fluff detector" that flags vague phrases (like "hard-working, passionate") in grammarIssues and suggests stronger alternatives.
                6. Explicitly call out missing resume sections (e.g., Summary, Projects, Skills, Certifications) and tell the user why they matter for this role.
                7. Check for basic Contact Information (Name, Email, Phone/Mobile, Location, LinkedIn/Portfolio). If the Target Role is technical (e.g., developer, engineer, data), explicitly look for a GitHub profile link (github.com/username) or portfolio.
                8. Provide a quantitative fit score (0-100) and sub-scores.

                ### STRICT CONSTRAINTS
                - **Role Family Detection**: From the resume and target job role, infer one broad family (Tech, Business, Education, Design/Creative, Support/Service, Other) and tailor strengths, missing skills, and advice to that family. Never suggest skills from another family.
                - **Tone & Format**: Use neutral, recruiter-style language. Standardize bullets to read like real HR feedback with full sentences. Include 1-2 India-focused examples (like "Java Developer at TCS" or "Sales Executive in FMCG (Mumbai)") if relevant.
                - **Top Fixes**: After the Executive Summary, output exactly three short bullets titled 'Top 3 things to fix first' that a student or jobseeker can do in the next 7 days. Each bullet should be very short and actionable (e.g., 'Add at least one internship or project related to [role].').
                - **Strengths**: Always provide exactly 3 bullets, each 1 line.
                - **Actionable Edits**: Start with ONE improved professional summary tailored to the role. Then suggest 3-5 entirely new or heavily improved bullets tailored to the JD. Provide "Before: ... -> After: ..." examples showing quantified impact.
                - **Missing Skills Format**: Group missing or weak keywords from the JD as 'Must-have' or 'Nice-to-have'. Prefix with '[Must-have]' or '[Nice-to-have]'. Mention explicitly WHERE the user should add them (e.g., Summary, Experience, Skills section). Max 3-5 items.
                - **Strategic Advice Format**: Explicitly mention missing sections (Summary, Projects, Skills, Certifications) and explain why they matter. Add explicit time frames (e.g., "In the next 1-3 months, do X", "In 6-12 months, complete Y", "In 1-2 years, build Z"). Keep them short and actionable.

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
                  "summary": "Professional summary (max 3 sentences). Always mention the target role family: 'Overall fit for entry-level teacher roles...' / 'for sales roles...' / 'for backend developer roles...'.",
                  "topFixes": ["Top 3 things to fix first as short actionable bullets"],
                  "strengths": ["List of exactly 3 distinct key strengths, each 1 short line. If a GitHub profile is found for a technical role, include 'GitHub profile detected – good for demonstrating real projects.' as one of the strengths."],
                  "missingSkills": ["List 3-5 keywords/skills missing. Pattern: '[Must-have] Keyword - Add to [Skills/Experience section]. Reason: ...'"],
                  "missingContactInfo": ["List of missing or hard-to-find contact elements like 'Mobile Number', 'Email'. If LinkedIn is missing, add: 'Add a LinkedIn profile link for professional networking.' If GitHub is not found and the target job is technical, add: 'Add a GitHub profile link showcasing your projects; it increases trust for technical roles.' Return an empty list if no issues."],
                  "actionableEdits": ["Suggest 3-5 new/rewritten bullets. Format: 'Before: [fluffy bullet] -> After: [quantified bullet]'. If none, suggest completely new bullets."],
                  "sectionFeedback": [
                      {
                        "sectionName": "Summary",
                        "good": ["2-3 bullets on what's good"],
                        "improvements": ["2-3 bullets on what to improve"]
                      },
                      {
                        "sectionName": "Experience",
                        "good": ["2-3 bullets on what's good"],
                        "improvements": ["2-3 bullets on what to improve"]
                      }
                  ],
                  "grammarIssues": ["Flag vague buzzwords/fluff like 'passionate, hard-working' and suggest replacements. Also list grammar/spelling errors. Or 'None'"]
                }

                ### INPUT DATA
                JOB ROLE: %s
                JOB DESCRIPTION (Priority):
                %s

                RESUME TEXT:
                %s
                """
                .formatted(perspectiveInstruction, jobRole, comparisonContext, jobRole,
                        (jobDescription != null && !jobDescription.isBlank() ? jobDescription : "Not Provided"),
                        resumeText);
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
