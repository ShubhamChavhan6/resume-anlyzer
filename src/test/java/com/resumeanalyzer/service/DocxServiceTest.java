package com.resumeanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class DocxServiceTest {

    private DocxService docxService;

    @BeforeEach
    void setUp() {
        docxService = new DocxService();
    }

    @Test
    void extractTextFromDocx_nullFile_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> docxService.extractTextFromDocx(null));
    }

    @Test
    void extractTextFromDocx_emptyFile_throwsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.docx", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> docxService.extractTextFromDocx(emptyFile));
    }

    @Test
    void extractTextFromDocx_notDocxContent_throwsException() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "not a docx".getBytes());
        assertThrows(Exception.class, () -> docxService.extractTextFromDocx(invalidFile));
    }
}