package com.resumeanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
    }

    @Test
    void extractTextFromPdf_nullFile_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> pdfService.extractTextFromPdf(null));
    }

    @Test
    void extractTextFromPdf_emptyFile_throwsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> pdfService.extractTextFromPdf(emptyFile));
    }

    @Test
    void extractTextFromPdf_invalidContentType_throwsException() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        assertThrows(IllegalArgumentException.class, () -> pdfService.extractTextFromPdf(invalidFile));
    }

    @Test
    void extractTextFromPdf_notPdfContent_throwsException() {
        MockMultipartFile nonPdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "not a pdf".getBytes());
        assertThrows(Exception.class, () -> pdfService.extractTextFromPdf(nonPdfFile));
    }

    @Test
    void extractImagesFromPdf_notPdfContent_throwsException() {
        MockMultipartFile nonPdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "not a pdf".getBytes());
        assertThrows(Exception.class, () -> pdfService.extractImagesFromPdf(nonPdfFile));
    }
}