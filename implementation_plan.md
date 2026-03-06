# Implementation Plan - Full-Stack AI Resume Analyzer

## Phase 1: Project Initialization
- Scaffold a Spring Boot project using Maven.
- **Java Version**: 21
- **Dependencies**:
  - `spring-boot-starter-web`: For REST APIs.
  - `lombok`: To reduce boilerplate code.
  - `pdfbox` (Apache): For PDF text extraction.
  - `spring-boot-devtools`: For hot reloading (optional but helpful).

## Phase 2: Architecture & Backend Logic
### Layered Architecture
1. **Controller Layer** (`ResumeController.java`)
   - Endpoint: `POST /api/resume/upload`
   - Accepts: `MultipartFile` (PDF)
   - Returns: JSON analysis of the resume.

2. **Service Layer**
   - `ResumeService.java`:
     - Handles the `MultipartFile`.
     - Uses `PDFTextStripper` inside a try-with-resources block to extract text from the PDF.
   - `AiService.java`:
     - Constructs the prompt for the AI.
     - Calls the Gemini 3 API using `RestTemplate`.
     - **Constraint**: `GEMINI_API_KEY` must be loaded from Environment Variables.

3. **Model/DTO Layer**
   - `AnalysisResponse.java`: Structured Record/Class to hold the AI's analysis (e.g., Summary, Skills, Improvements).

## Phase 3: Frontend Development (Glassmorphism)
- **Location**: `src/main/resources/static/`
- `index.html`:
  - Clean layout with a "Glass" container.
  - File upload drag-and-drop zone.
  - Analysis result display area.
- `style.css`:
  - **Glassmorphism**: Translucent background, blur variance, subtle borders.
  - **Animations**: Loading spinners, fade-in results.
- `script.js`:
  - Handle file selection.
  - Async `fetch` call to the backend.
  - Render Markdown/Text response beautifully.

## Phase 4: Integration & Security
- **API Key Management**: 
  - Do not hardcode keys.
  - Use `System.getenv("GEMINI_API_KEY")` or Spring's `@Value("${GEMINI_API_KEY}")`.
- **Error Handling**: Global exception handler for invalid files or API failures.

## Phase 5: Verification Strategy
1. **Build**: Run `mvn clean install` to verify dependencies.
2. **Run**: Start the application via `mvn spring-boot:run` on port 8080.
3. **Browser Test**:
   - Open browser subagent.
   - Navigate to `http://localhost:8080`.
   - Upload a sample resume PDF.
   - Validate that the AI response is displayed correctly on the UI.
