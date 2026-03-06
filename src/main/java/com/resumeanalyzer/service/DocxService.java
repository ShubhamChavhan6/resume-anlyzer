package com.resumeanalyzer.service;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DocxService {

    public String extractTextFromDocx(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded DOCX file is empty or missing.");
        }

        try (InputStream is = file.getInputStream();
                XWPFDocument document = new XWPFDocument(is);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            if (text == null || text.isBlank() || text.trim().length() < 50) {
                return "";
            }
            return text.trim();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process DOCX file: " + e.getMessage());
        }
    }
}
