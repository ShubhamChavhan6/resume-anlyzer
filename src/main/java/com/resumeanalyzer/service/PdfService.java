package com.resumeanalyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {

    public String extractTextFromPdf(MultipartFile file) {
        // 1. Validate Input
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded PDF file is empty or missing.");
        }

        // 2. Validate Content Type (Basic Check)
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Please upload a PDF.");
        }

        // 3. Extract Text using PDFBox
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("Encrypted/Password-protected PDFs are not supported.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank() || text.trim().length() < 50) {
                return ""; // Indicating OCR is needed
            }

            return text.trim();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF file: " + e.getMessage());
        }
    }

    public java.util.List<byte[]> extractImagesFromPdf(MultipartFile file) {
        java.util.List<byte[]> images = new java.util.ArrayList<>();
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
            int pages = document.getNumberOfPages();
            int maxPages = Math.min(pages, 3); // Limit to first 3 pages for speed/cost
            for (int i = 0; i < maxPages; i++) {
                java.awt.image.BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150,
                        org.apache.pdfbox.rendering.ImageType.RGB);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bim, "jpeg", baos);
                images.add(baos.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract images from PDF: " + e.getMessage());
        }
        return images;
    }
}
