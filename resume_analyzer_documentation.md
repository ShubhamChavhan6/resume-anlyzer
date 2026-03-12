# Resume Analyzer Project Documentation

This document explains the structure, functionality, and the overall workflow of the Resume Analyzer project.

## 1. Project Overview
**Resume Analyzer** is a Spring Boot web application designed to analyze user resumes against a specific job role and description. It evaluates the resume based on criteria from different reviewer perspectives (e.g., Technical Interviewer, HR) and provides actionable feedback, a score, and suggestions for improvement using AI integration.

---

## 2. Directory Structure
The project is organized following typical Maven and Spring Boot structures:

```
resume-anlyzer/
├── src/
│   ├── main/
│   │   ├── java/com/resumeanalyzer/
│   │   │   ├── ResumeAnalyzerApplication.java   # Main Spring Boot entry point
│   │   │   ├── controller/                      # Handles incoming HTTP requests
│   │   │   ├── service/                         # Business logic, file parsing, AI API calls
│   │   │   └── model/                           # Data Transfer Objects (DTOs) for request/response
│   │   └── resources/
│   │       ├── application.properties           # App configuration (e.g., server port, API keys)
│   │       └── static/                          # Frontend files
│   │           ├── index.html                   # Main UI structure
│   │           ├── style.css                    # UI styling
│   │           └── script.js                    # UI logic and API calls
├── pom.xml                                      # Maven dependencies and build config
└── ...                                          # Other documentation and config files
```

---

## 3. Core Backend Components

### A. Controllers (`com.resumeanalyzer.controller`)
*   **`ResumeController.java`**: The main REST API endpoint (`/api/resume/analyze`). It receives `POST` requests containing either a resume file (PDF, DOCX, Image) or pasted text, along with job details (Role, Description) and Reviewer Type. It passes this data to the `ResumeService` and returns the AI evaluation as an `AnalysisResponse`.

### B. Services (`com.resumeanalyzer.service`)
*   **`ResumeService.java`**: The orchestrator. It checks the input type (file vs. pasted text) and delegates the text extraction to the appropriate specialized service (`PdfService`, `DocxService`, or `AiService` for images). Once text is extracted, it passes the text to `AiService` for evaluation.
*   **`PdfService.java`**: Responsible for parsing text from PDF files. If a PDF is image-based, it extracts images and falls back to OCR.
*   **`DocxService.java`**: Responsible for parsing text from Microsoft Word (`.docx`) files.
*   **`AiService.java`**: This service communicates with the external AI provider (like Google Gemini or OpenAI). It performs two major functions:
    1.  Image OCR (Extracting text from images).
    2.  Analyzing the final extracted text against the provided job role/description and formatting the response into the `AnalysisResponse` object.

### C. Models (`com.resumeanalyzer.model`)
*   **`AnalysisRequest.java`**: Represents the incoming data request structure.
*   **`AnalysisResponse.java`**: A structured Java object representing the outcome of the AI analysis (e.g., Score, Strengths, Weaknesses, Suggestions).

---

## 4. Frontend Components (`src/main/resources/static/`)

*   **`index.html`**: Provides the form where users can upload their resume, paste text, select target job roles, and specify the type of reviewer they want feedback from.
*   **`style.css`**: Contains styles to make the application look modern and responsive.
*   **`script.js`**: Handles form submission via Javascript. It captures the user input, constructs a `FormData` object, sends an asynchronous `fetch` request to the backend (`/api/resume/analyze`), and dynamically updates the DOM with the received analysis results (score, charts, feedback).

---

## 5. How It Works (The Full Workflow)

1.  **User Input:** The user opens the web page (`index.html`), fills out the form (uploads a resume file or pastes text, enters job details), and clicks "Analyze".
2.  **Frontend Request:** `script.js` intercepts the form submission, packages the file and text data, and sends a `POST /api/resume/analyze` request to the backend.
3.  **Controller Routing:** The `ResumeController` receives the multipart request and passes the data to the `ResumeService`.
4.  **Text Extraction:** 
    *   `ResumeService` inspects the input. If it's pure text, it skips extraction.
    *   If it's a file, it checks the MIME type/extension.
    *   It uses `PdfService` for PDFs, `DocxService` for Word docs, or `AiService` (OCR) for Images to extract the raw text of the resume.
5.  **AI Analysis:** The raw text, along with the job role and description, is sent to `AiService`. This service constructs a prompt and calls the external LLM API.
6.  **Response Generation:** The LLM evaluates the resume and returns a structured response (Score, Feedback, Missing Info). The `AiService` converts this JSON into an `AnalysisResponse` Java object.
7.  **Frontend Display:** The Controller returns the `AnalysisResponse` back to `script.js` as JSON. The Javascript parses this JSON and renders the score (often with a visual indicator/chart) and detailed feedback onto the webpage for the user to read.
