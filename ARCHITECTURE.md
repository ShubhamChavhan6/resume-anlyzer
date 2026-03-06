# Architecture Documentation - AI Resume Analyzer

## 1. System Overview
The **AI Resume Analyzer** is a Full-Stack Web Application designed to evaluate resumes against modern industry standards using Generative AI (Google Gemini 1.5 Flash). The system follows a **Monolithic Layered Architecture** using Spring Boot for the backend and Vanilla HTML/CSS/JS for the frontend.

### Key Features
- **PDF Ingestion**: Robust text extraction from PDF documents using Apache PDFBox.
- **AI Analysis**: High-speed, context-aware analysis using Gemini 1.5 Flash.
- **Glassmorphism UI**: A trusted, modern, and aesthetic user interface.
- **Stateless Design**: No database required; analysis happens on-the-fly for maximum privacy.

---

## 2. Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Backend** | Java | 21 | Core Logic |
| **Framework** | Spring Boot | 3.4.0 | Web Server & DI |
| **Build Tool** | Maven | 3.9.x | Dependency Management |
| **PDF Engine** | Apache PDFBox | 3.0.0 | Text Extraction |
| **AI Model** | Gemini 1.5 Flash | v1beta | Generative Intelligence |
| **Frontend** | HTML5 / CSS3 | - | Structure & Styling |
| **Scripting** | Vanilla JS | ES6+ | Client-side Interactivity |

---

## 3. Backend Architecture
The backend is structured into distinct layers to enforce Separation of Concerns (SoC) while performing a "No-Fluff" philosophy suitable for interviews.

### 3.1. Controller Layer (`ResumeController.java`)
- **Role**: Entry point for HTTP requests.
- **Endpoint**: `POST /api/resume/analyze`
- **Responsibility**: 
  - Validates the incoming `MultipartFile`.
  - Orchestrates calls to `ResumeService`.
  - Global Exception Handling is managed by `GlobalExceptionHandler` to keep the controller clean.

### 3.2. Service Layer
This layer holds the business logic and external integrations.

#### A. `PdfService.java` (Business Logic)
- **Role**: Specialized service for PDF handling.
- **Input**: Raw PDF Byte Stream.
- **Process**: 
  - Validates file types and content.
  - Loads the document into `PDDocument` (Apache PDFBox).
  - Strips text using `PDFTextStripper`.
- **Output**: Cleaned raw String text of the resume.

#### B. `ResumeService.java` (Orchestrator)
- **Role**: Coordinates the analysis flow.
- **Process**:
  - call `PdfService` to extract text.
  - Validates text length.
  - Calls `AiService` for the final analysis.

#### C. `AiService.java` (AI Integration)
- **Role**: Manages all interactions with the Gemini API.
- **Components Merged**: Includes the Logic of the former `PromptBuilder` class.
- **Process**:
  - **Dynamic Model Discovery**: Automatically finds the best available Gemini model ("Flash" > "Pro").
  - **Prompt Engineering**: Wraps the resume text with a strict "Technical Recruiter" persona.
  - **API Call**: Uses `RestTemplate` to query the model.
  - **Sanitization**: Cleans markdown code blocks from the JSON response.
- **Output**: `AnalysisResponse` object.

### 3.3. Model Layer (`AnalysisResponse.java`)
A Java `record` (immutable data carrier) that defines the contract between Backend and Frontend. It replaces the traditional "DTO" pattern for simplicity.
```java
public record AnalysisResponse(
    int score,
    List<String> missingSkills,
    String suggestions,
    List<String> grammarIssues
) {}
```

---

## 4. Frontend Architecture
The frontend is designed to be lightweight and fast, serving static assets directly from Spring Boot's embedded Tomcat.

### 4.1. Design System (Glassmorphism)
- **Variables**: CSS Variables (`--primary`, `--glass-bg`) manage the theme.
- **Visuals**: 
  - Background "Blobs" with CSS animations (`@keyframes float`).
  - Translucent cards using `backdrop-filter: blur(16px)`.
  - Interactive states (hover, drag-over) for better UX.

### 4.2. Client Logic (`script.js`)
- **Event Listeners**: Handles Drag & Drop events (`drop`, `dragover`) and File Input changes.
- **Async Communication**: Uses `fetch` API to send `FormData` to the backend.
- **Dynamic Rendering**:
  - Injects HTML directly for badges (Skills).
  - Renders the Score Visualization.
  - Displays the "Improvements" section converting the Markdown response to HTML.

---

## 5. Data Flow Diagram

1. **User** uploads PDF -> **Frontend** (Drag & Drop)
2. **Frontend** sends `POST /analyze` -> **Backend Controller**
3. **Controller** delegates to `ResumeService`.
4. **ResumeService** calls `PdfService` -> **Extracts Text**
5. **ResumeService** calls `AiService` -> **AI Analysis**
6. **AiService** builds prompt -> **Calls Gemini API**
7. **Gemini API** returns JSON Analysis -> **AiService** cleans & maps it
8. **Controller** returns `AnalysisResponse` -> **Frontend**
9. **Frontend** renders results on the UI.
