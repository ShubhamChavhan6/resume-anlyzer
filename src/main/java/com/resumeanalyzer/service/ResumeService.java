package com.resumeanalyzer.service;

import com.resumeanalyzer.model.AnalysisResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private final PdfService pdfService;
    private final DocxService docxService;
    private final AiService aiService;

    public ResumeService(PdfService pdfService, DocxService docxService, AiService aiService) {
        this.pdfService = pdfService;
        this.docxService = docxService;
        this.aiService = aiService;
    }

    public AnalysisResponse analyze(MultipartFile file, String pastedText, String jobRole, String jobDescription,
            String reviewerType) {
        String resumeText = "";

        if (pastedText != null && !pastedText.trim().isEmpty()) {
            resumeText = pastedText.trim();
        } else if (file != null && !file.isEmpty()) {
            try {
                String contentType = file.getContentType();
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

                // 1. Direct Image OCR
                if (contentType != null && contentType.startsWith("image/")) {
                    resumeText = aiService.extractTextFromImage(file.getBytes(), contentType);
                }
                // 2. DOCX Processing
                else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)
                        || filename.endsWith(".docx")) {
                    resumeText = docxService.extractTextFromDocx(file);
                }
                // 3. PDF Processing
                else if ("application/pdf".equals(contentType) || filename.endsWith(".pdf")) {
                    resumeText = pdfService.extractTextFromPdf(file);

                    // OCR Fallback if PDF text extraction fails or returns too little
                    if (resumeText.length() < 200) {
                        java.util.List<byte[]> images = pdfService.extractImagesFromPdf(file);
                        StringBuilder ocrText = new StringBuilder();
                        for (byte[] img : images) {
                            ocrText.append(aiService.extractTextFromImage(img, "image/jpeg")).append("\n");
                        }
                        if (ocrText.length() > 50) {
                            resumeText = ocrText.toString().trim();
                        }
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported file format. Please upload a PDF, DOCX, or Image (JPG/PNG).");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error reading resume file: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Please provide either a pasted resume text or a valid file.");
        }

        // 3. Final validation
        if (resumeText.length() < 50) {
            throw new IllegalArgumentException(
                    "We couldn't read the text from this resume. Please upload a clearer file or paste your resume text directly.");
        }

        // 4. AI Analysis
        return aiService.analyzeResume(resumeText, jobRole, jobDescription, reviewerType);
    }
}
