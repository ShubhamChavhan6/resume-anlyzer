package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResponse;
import com.resumeanalyzer.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*") // In production, restrict this to specific domains
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResponse> analyzeResume(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "pastedText", required = false) String pastedText,
            @RequestParam(value = "jobRole", defaultValue = "Software Engineer") String jobRole,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            @RequestParam(value = "reviewerType", defaultValue = "Technical Interviewer") String reviewerType) {
        // Validation handled by Service & Util. GlobalExceptionHandler catches errors.
        AnalysisResponse response = resumeService.analyze(file, pastedText, jobRole, jobDescription, reviewerType);
        return ResponseEntity.ok(response);
    }
}
