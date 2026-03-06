package com.resumeanalyzer.model;

/**
 * Incoming Request DTO.
 * Used if we expand to handle metadata (like job role) in the future.
 */
public record AnalysisRequest(
                String jobRole,
                String reviewerType) {
}
