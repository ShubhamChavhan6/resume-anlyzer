package com.resumeanalyzer.model;

import java.util.List;

/**
 * Standard API Response DTO.
 * This record maps strictly to the JSON output from the AI.
 */
public record AnalysisResponse(
                int score,
                int skillsScore,
                int experienceScore,
                int formattingScore,
                int clarityScore,
                String summary,
                List<String> topFixes,
                List<String> strengths,
                List<String> missingSkills,
                List<String> missingContactInfo,
                List<String> actionableEdits,
                List<SectionFeedback> sectionFeedback,
                List<String> grammarIssues) {

        public record SectionFeedback(
                        String sectionName,
                        List<String> good,
                        List<String> improvements) {
        }
}
