package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResponse;
import com.resumeanalyzer.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeService resumeService;

    @Test
    void analyzeResume_withPastedText_returnsOk() throws Exception {
        AnalysisResponse mockResponse = new AnalysisResponse(
            85, 90, 80, 75, 80, "Good candidate",
            List.of("Fix 1"),
            List.of("Strength 1"),
            List.of("Skill"),
            List.of("Email"),
            List.of("Edit"),
            List.of(),
            List.of()
        );

        when(resumeService.analyze(any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/resume/analyze")
                .param("pastedText", "John Doe\nDeveloper")
                .param("jobRole", "Backend Developer")
                .param("reviewerType", "Technical"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(85));
    }

    @Test
    void analyzeResume_withFile_returnsOk() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "resume.pdf", "application/pdf", "test".getBytes());

        AnalysisResponse mockResponse = new AnalysisResponse(
            80, 85, 75, 70, 80, "Good fit",
            List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(), List.of()
        );

        when(resumeService.analyze(any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/resume/analyze")
                .file(pdfFile)
                .param("jobRole", "Developer"))
            .andExpect(status().isOk());
    }

    @Test
    void analyzeResume_noInput_returnsBadRequest() throws Exception {
        when(resumeService.analyze(any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Please provide either a pasted resume text or a valid file."));

        mockMvc.perform(multipart("/api/resume/analyze")
                .param("jobRole", "Developer"))
            .andExpect(status().isBadRequest());
    }
}