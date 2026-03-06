# ResumeAI - Professional Resume Analyzer

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-green.svg)

## 📖 Overview

**ResumeAI** is a powerful, full-stack application designed to help job seekers optimize their resumes. By leveraging the **Google Gemini AI API**, the application analyzes PDF resumes against specific job roles (e.g., "Java Developer", "Product Manager") and provides detailed, actionable feedback.

The application simulates reviews from two distinct perspectives:
*   **HR Recruiter**: Focuses on formatting, soft skills, and clarity.
*   **Technical Interviewer**: Focuses on technical depth, accuracy, and project impact.

## 🛠️ Technology Stack

*   **Backend**: Java 21, Spring Boot 3.4.0 (Web, Actuator)
*   **AI Engine**: Google Gemini 1.5 Pro (via Gemini REST API)
*   **PDF Processing**: Apache PDFBox 3.0.0
*   **Frontend**: HTML5, Vanilla JavaScript, CSS3 (Custom Glassmorphism Design)
*   **Build Tool**: Apache Maven 3.9.12

## 📂 Project Structure

Here is a guide to the key files and directories in the project:

```text
resume-anlyzer/
├── src/main/java/com/resumeanalyzer/
│   ├── controller/
│   │   └── ResumeController.java       # REST API endpoints for file upload & analysis
│   ├── service/
│   │   ├── ResumeService.java          # Orchestrates PDF parsing and AI service calls
│   │   ├── PdfService.java             # Extracts raw text from PDF files using PDFBox
│   │   └── AiService.java              # Communicates with Google Gemini API
│   ├── model/
│   │   └── AnalysisResponse.java       # Structure of the JSON response sent to frontend
│   │   └── AnalysisRequest.java        # Request object for the analysis (file + role)
│   ├── exception/
│   │   └── GlobalExceptionHandler.java # Centralized error handling
│   ├── util/
│   │   └── PromptBuilder.java          # Generates prompt templates for the AI
│   └── ResumeAnalyzerApplication.java  # Main entry point for the Spring Boot app
│
├── src/main/resources/
│   ├── static/                         # Frontend Assets
│   │   ├── index.html                  # Main user interface
│   │   ├── style.css                   # Custom CSS styling (Dark Theme/Glassmorphism)
│   │   └── script.js                   # Client-side logic (Drag & drop, API calls)
│   └── application.properties          # Config (Port, File limits, App name)
│
├── ARCHITECTURE.md                     # High-level architectural diagrams and flow
├── APP_WORKFLOWS.md                    # Detailed user and technical workflows
├── pom.xml                             # Maven dependency configuration
└── README.md                           # This file
```

## 🚀 How to Run

### Prerequisites
*   Java Development Kit (JDK) 21 or higher.
*   Maven (Wrapper is included, or use installed version).
*   **Google Gemini API Key**: You need a valid API key from Google AI Studio.

### Steps

1.  **Set Environment Environment**:
    You must set your Gemini API key as an environment variable for security.
    ```powershell
    # Windows PowerShell
    $env:GEMINI_API_KEY = "your_actual_api_key_here"
    ```

2.  **Run the Application**:
    Navigate to the project root and run:
    ```powershell
    ./mvnw spring-boot:run
    # OR if you have maven installed globally
    mvn spring-boot:run
    ```

3.  **Access the UI**:
    Open your browser and go to:
    **[http://localhost:8080](http://localhost:8080)**

## ✨ Features

1.  **Drag & Drop Interface**: Easy file upload for PDF resumes.
2.  **Job Description Comparison**: Paste the full JD to compare your skills directly against specific constraints.
3.  **Role-Specific Analysis**: Tailors feedback specifically to the target job title.
4.  **Dual Perspectives**: Choose between HR or Technical feedback styles.
5.  **Smart Gap Analysis**: Detects critical missing skills based on the comparison.
6.  **Instant Scoring**: Visual score indicator (0-100) based on relevance.
7.  **Detailed Breakdown**:
    *   **Actionable Edits**: Concrete "Rewrite X as Y" suggestions.
    *   **Missing Skills**: Identifies critical keywords missing from the resume.
    *   **Grammar & Formatting**: Checks for unprofessional errors.
    *   **Strategic Suggestions**: High-level advice to improve impact.

## 📝 Configuration

You can configure application settings in `src/main/resources/application.properties`:

*   `server.port`: Change the running port (default: 8080).
*   `spring.servlet.multipart.max-file-size`: Adjust max upload size (default: 5MB).

---
*Built for Resume Analyzer*
