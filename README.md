# ResumeAI - Professional Resume Analyzer

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-green.svg)

## 📖 Overview

**ResumeAI** is a powerful Spring Boot full-stack application designed to help job seekers optimize their resumes. By leveraging the **Google Gemini AI API**, the application analyzes PDF or Word resumes against specific job roles (e.g., "Java Developer", "Product Manager") and provides detailed, actionable feedback.

The application simulates reviews from two distinct perspectives:
*   **HR Recruiter**: Focuses on formatting, soft skills, and clarity.
*   **Technical Interviewer**: Focuses on technical depth, accuracy, and project impact.

It features a stateless design where analysis happens on-the-fly for maximum privacy, and no data is stored post-analysis.

## 🛠️ Technology Stack & Architecture

The system follows a **Monolithic Layered Architecture**:

*   **Backend**: Java 21, Spring Boot 3.4.0 (Web, Actuator)
*   **AI Engine**: Google Gemini 1.5 Pro/Flash (via Gemini REST API)
*   **Document Processing**: Apache PDFBox 3.0.0 (PDFs), Apache POI (Word Docs)
*   **Frontend**: HTML5, Vanilla JavaScript, CSS3 (Glassmorphism UI)
*   **Build Tool**: Apache Maven 3.9.12

### Data Flow
1. **Frontend**: User drops a file (PDF/DOCX) or pastes text into the UI. `script.js` sends it via `POST /api/resume/analyze`.
2. **Controller Layer** (`ResumeController.java`): Receives the multipart request and routes it.
3. **Extraction Services**: `ResumeService` uses `PdfService`, `DocxService`, or `AiService` (for OCR on images) to extract the text.
4. **AI Analysis**: `AiService` queries the Gemini API with a strict "Technical Recruiter" or "HR" persona prompt.
5. **Response**: The AI response is mapped to the `AnalysisResponse` record and sent back to the frontend, which dynamically renders the scores and feedback.

## 📂 Project Structure

```text
resume-anlyzer/
├── src/main/java/com/resumeanalyzer/
│   ├── controller/             # REST APIs (ResumeController, SeoController, HealthController)
│   ├── service/                # Business logic, extraction, AI interactions
│   ├── model/                  # Request/Response data records (AnalysisResponse)
│   └── ResumeAnalyzerApplication.java
│
├── src/main/resources/
│   ├── static/                 # Frontend Assets (index.html, style.css, script.js, SEO files)
│   └── application.properties  # Config (Port, API keys, limits)
│
├── pom.xml                     # Maven dependencies
└── README.md                   # Project documentation
```

## ✨ Core Features

1.  **Multi-Format Upload**: Supports PDF, DOCX, Images (via OCR), and raw text pasting.
2.  **Job Description Comparison**: Compare skills directly against pasting targeting JDs.
3.  **Role-Specific Analysis**: Dynamic feedback targeting the desired role.
4.  **Actionable Edits**: Concrete "Rewrite A as B" suggestions for resume bullet points.
5.  **Instant Scoring & Missing Skills**: Real-time visual scoring (0-100) and gap analysis.

## 🚀 How to Run Locally

### Prerequisites
*   Java Development Kit (JDK) 21 or higher.
*   A **Google Gemini API Key** from Google AI Studio.

### Steps
1.  **Configure API Key**:
    Open `src/main/resources/application.properties` and add your key:
    ```properties
    GEMINI_API_KEY=your_actual_api_key_here
    ```
    *Alternatively, set it as an environment variable (`$env:GEMINI_API_KEY="..."`).*

2.  **Run the Application**:
    Navigate to the project root and start the server using Maven:
    ```powershell
    ./mvnw spring-boot:run
    ```

3.  **Access the UI**:
    Open your browser and navigate to: **[http://localhost:8080](http://localhost:8080)**

## 🛠 Troubleshooting
*   **"Resume content is too short"**: Ensure the PDF is text-based. If it's an image, the OCR service will try to parse it, but low-res images may fail.
*   **"Ambiguous handler methods / Port already in use"**: Make sure you have terminated any running instances before starting `spring-boot:run`.
*   **"Service Unavailable / Quota Exceeded"**: Ensure your Google API Key is valid and hasn't hit free-tier rate limits.
