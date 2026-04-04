package com.resumeanalyzer.service;

import com.resumeanalyzer.model.AnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumeServiceTest {

    @Mock
    private PdfService pdfService;

    @Mock
    private DocxService docxService;

    @Mock
    private AiService aiService;

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(pdfService, docxService, aiService);
    }

    @Test
    void analyze_noFileNoText_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            resumeService.analyze(null, null, "Developer", null, "Technical"));
    }

    @Test
    void analyze_emptyFileAndText_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            resumeService.analyze(new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]), 
                "", "Developer", null, "Technical"));
    }

    @Test
    void analyze_unsupportedFileType_throwsException() {
        MockMultipartFile textFile = new MockMultipartFile("file", "resume.txt", "text/plain", "test".getBytes());
        
        assertThrows(RuntimeException.class, () -> 
            resumeService.analyze(textFile, null, "Developer", null, "Technical"));
    }

    @Test
    void analyze_resumeTextTooShort_throwsException() {
        when(pdfService.extractTextFromPdf(any())).thenReturn("Short");

        MockMultipartFile pdfFile = new MockMultipartFile("file", "resume.pdf", "application/pdf", "test".getBytes());
        
        assertThrows(IllegalArgumentException.class, () -> 
            resumeService.analyze(pdfFile, null, "Developer", null, "Technical"));
    }
}