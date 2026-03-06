# Application Workflows

## 1. User Interaction Workflow
This describes the experience from the end-user's perspective.

### Step 1: Accessing the Application
- User opens a web browser.
- Navigates to `http://localhost:8080`.
- **Visual**: Sees a dark-themed, glassmorphism interface with floating ambient colors.

### Step 2: Uploading a Resume
- **Option A**: User drags a PDF file from their desktop and drops it into the "Drag & Drop" box.
- **Option B**: User clicks "Browse Files", selects a PDF via the OS dialog.
- **System Check**: Frontend verifies the file type is `.pdf`. If not, it alerts the user immediately.

### Step 3: Analysis (The "Magic" Moment)
- The upload box fades out.
- A loading spinner appears with the text "Analyzing your potential...".
- The user waits approx. 3-8 seconds (depending on PDF complexity and API latency).

### Step 4: Viewing Results
- The loading screen disappears.
- **Results Dashboard** slides in:
  - **Score**: A large number (0-100) indicating resume quality.
  - **Summary**: A generated professional bio.
  - **Skills**: Green badges showing detected technologies/skills.
  - **Missing Skills**: Red badges highlighting what is missing for the target role.
  - **Improvements**: Detailed text advice on how to fix the resume.
- **Action**: User can click "Analyze Another" to reset the flow.

---

## 2. Technical Execution Workflow (Internal)
What happens under the hood when a file is uploaded.

### Phase 1: Ingestion & Extraction
1. **Request**: HTTP POST request carries the file binary.
2. **Temp Storage**: Java creates a randomized `.tmp` file (e.g., `resume8293.pdf`).
3. **PDFBox Loading**: `Loader.loadPDF()` opens the file.
4. **Stripping**: `PDFTextStripper` iterates through pages and pulls raw strings.
   - *Note*: Images and formatting are ignored; we only care about semantic content.
5. **Cleanup**: `finally` block deletes the `.tmp` file to prevent disk clutter.

### Phase 2: Intelligence Processing
1. **Prompt Construction**: The system injects the raw text into a template:
   > "Analyze this resume... output strictly JSON... keys: score, summary..."
2. **Key Validation**: Checks if `GEMINI_API_KEY` is present.
   - If `demo-key`, returns a mock response immediately.
   - If valid, prepares the HTTP JSON payload.
3. **API Turnaround**: 
   - Sends payload to `generativelanguage.googleapis.com`.
   - Waits for response.
4. **Parsing Strategy**:
   - The AI often returns Markdown code blocks (e.g., ```json ... ```).
   - The `AiService` uses string manipulation to strip these markers.
   - `Jackson ObjectMapper` converts the remaining string into the `AnalysisResponse` Java Object.

### Phase 3: Response Delivery
1. **Serialization**: Spring Boot automatically converts the Java Object -> JSON.
2. **Network**: JSON travels back to the browser.
3. **DOM Manipulation**: JavaScript receives the JSON and updates the `innerText` and `innerHTML` of specific DIVs.

---

## 3. Developer Workflow
How to maintain and run the project.

### Setup
1. **Clone/Open**: Open the folder in VS Code.
2. **Env Var**: Set `$env:GEMINI_API_KEY = "AIzaSy..."` in PowerShell.
3. **Dependencies**: Run `mvn clean install` to download libraries.

### Running
- **Command**: `mvn spring-boot:run`
- **Port**: 8080 (Default)
- **Hot Reload**: `spring-boot-devtools` is included. Recompiling a file (Ctrl+Shift+B or auto-save) will trigger a fast restart.

### Troubleshooting
- **"mvn not found"**: Ensure Maven bin is in PATH or use the local `apache-maven-x.x.x` folder.
- **"API Key Missing"**: The app will run in Demo Mode. Check console logs for environment variable status.
