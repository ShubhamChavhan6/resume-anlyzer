# Resume Analyzer - Project Guide

## 📌 Project Overview
The **Resume Analyzer** is a web application that uses Artificial Intelligence (Google Gemini) to review resumes. It mimics a professional Technical Recruiter, analyzing your resume against a specific job role and providing detailed feedback, including scores, missing skills, and strategic improvement suggestions.

### 🌟 Key Features
- **AI-Powered Analysis**: Uses Google Gemini to understand context, not just keywords.
- **Dynamic Model Selection**: Automatically finds the best available AI model for your API key (Flash, Pro, etc.).
- **Recruiter Persona**: The AI adopts the persona of a strict Technical Recruiter to give realistic feedback.
- **PDF Parsing**: extract text from PDF resumes securely.
- **Modern UI**: Clean, responsive interface with drag-and-drop upload.

---

## 📂 Project Structure
Here is a simplified explanation of the key files and folders:

### `src/main/resources/static` (Frontend)
- **`index.html`**: The main webpage. Contains the structure of the UI (Upload box, Results section).
- **`style.css`**: Defines the look and feel (Colors, spacing, "glass" effect).
- **`script.js`**: Handles the logic in your browser: sending the file to the server and displaying the results on the screen.

### `src/main/java/com/resumeanalyzer` (Backend Logic)
- **`controller/ResumeController.java`**: The "Traffic Cop". It receives the uploaded file from the frontend and sends it to the Service layer.
- **`service/ResumeService.java`**: The "Manager". It coordinates the process: first extracts text using `PdfUtil`, checks if the resume is valid, then asks `AiService` to analyze it.
- **`service/AiService.java`**: The "Brain". This connects to Google's Gemini API. It smartly picks the best AI model available and sends the resume text for analysis.
- **`util/PdfUtil.java`**: The "Translator". It reads the raw PDF file and converts it into plain text that the AI can read.
- **`util/PromptBuilder.java`**: The "Instruction Manual". It tells the AI exactly *how* to behave (e.g., "Act as a Technical Recruiter", "Output in JSON format").
- **`dto/AnalysisResponse.java`**: A simple container (box) that holds the result data (Score, Suggestions, etc.) to send back to the frontend.

### Configuration
- **`application.properties`**: Settings file where you define things like the Server Port (8080) and your Google API Key.
- **`pom.xml`**: The "Shopping List" for the project. It tells Maven which libraries to download (Spring Boot, PDFBox, etc.).

---

## ⚙️ How It Works (The Flow)

1.  **User Uploads**: You drag a PDF onto the webpage.
2.  **Frontend Sends**: `script.js` sends the file + Job Role to the backend (`/api/resume/analyze`).
3.  **Controller Receives**: `ResumeController` gets the request.
4.  **Text Extraction**: `ResumeService` uses `PdfUtil` to turn the PDF into text.
5.  **Prompt Creation**: `PromptBuilder` wraps the text in a detailed instruction: *"You are a recruiter. Analyze this for a [Job Role]..."*
6.  **AI Analysis**: `AiService` sends this prompt to Google Gemini.
    *   *Smart Feature*: It first checks which Gemini models you are allowed to use and picks the fastest one automatically.
7.  **Response Parsing**: The AI replies with JSON data. `AiService` cleans it up.
8.  **Display**: The frontend receives the JSON and updates the page with your Score, Missing Skills, and Report.

---

## 🚀 How to Run

### Prerequisites
- Java 21 or higher.
- Maven (included in this project wrapper).
- A Google Gemini API Key.

### Steps
1.  **Configure API Key**:
    Open `src/main/resources/application.properties` and add your key:
    ```properties
    GEMINI_API_KEY=your_actual_api_key_here
    ```

2.  **Run the App**:
    Open a terminal in the project folder and run:
    ```powershell
    mvn spring-boot:run
    ```

3.  **Access**:
    Open your browser and go to: `http://localhost:8080`

---

## 🛠 Troubleshooting
- **"Resume content is too short"**: The PDF might be an image. Ensure it's a text-based PDF.
- **"AI Service Unavailable"**: Check your API Key or Internet Connection. If you see "Quota Exceeded", you've hit the free tier limit.
- **"Model not found"**: The app auto-fixes this, but if it persists, ensure your Google Cloud project has access to Gemini.
