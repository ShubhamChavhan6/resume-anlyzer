// DOM Elements
const dropZone = document.getElementById('drop-zone');
const fileInput = document.getElementById('file-input');
const analyzeBtn = document.getElementById('analyze-btn');
const jobRoleInput = document.getElementById('job-role');
const uploadCard = document.getElementById('upload-card');
const loadingView = document.getElementById('loading-view');
const resultsView = document.getElementById('results-view');

let selectedFile = null;

// New Modal Elements
const howItWorksLink = document.getElementById('how-it-works');
const howItWorksModal = document.getElementById('how-it-works-modal');
const closeModal = document.getElementById('close-modal');
const closeModalBtn = document.getElementById('close-modal-btn');

// Text Paste / File Toggle Elements
const toggleFileBtn = document.getElementById('toggle-file-btn');
const togglePasteBtn = document.getElementById('toggle-paste-btn');
const pasteZone = document.getElementById('paste-zone');
const pasteInput = document.getElementById('paste-input');

pasteInput.addEventListener('input', checkFormState);

toggleFileBtn.addEventListener('click', () => {
    toggleFileBtn.className = 'btn-upload btn-upload-active';
    togglePasteBtn.className = 'btn-upload btn-upload-inactive';
    dropZone.classList.remove('hidden');
    pasteZone.classList.add('hidden');
    pasteInput.value = ''; // clear text if switching to file
    checkFormState();
});

togglePasteBtn.addEventListener('click', () => {
    togglePasteBtn.className = 'btn-upload btn-upload-active';
    toggleFileBtn.className = 'btn-upload btn-upload-inactive';
    pasteZone.classList.remove('hidden');
    dropZone.classList.add('hidden');
    selectedFile = null; // clear file if switching to text

    // Reset file UI
    fileInput.value = '';
    dropZone.innerHTML = `
        <div class="upload-icon-wrapper">
            <i data-lucide="upload-cloud" style="width: 32px; height: 32px; color: var(--primary);"></i>
        </div>
        <h3 style="color: var(--text-primary);">Drop your resume here</h3>
        <p class="text-secondary text-small" style="margin-top: 0.5rem;">or <span style="color: var(--primary); text-decoration: underline;">browse to upload</span></p>
        <p class="text-secondary" style="font-size: 0.85rem; margin-top: 1rem; font-weight: 500;">PDF, DOCX, JPG, PNG up to 5 MB</p>
        <input type="file" id="file-input" accept=".pdf,.docx,image/png,image/jpeg,image/jpg" class="hidden">
    `;
    lucide.createIcons();
    checkFormState();
});

howItWorksLink.addEventListener('click', (e) => {
    e.preventDefault();
    howItWorksModal.classList.remove('hidden');
});
const hideModal = () => howItWorksModal.classList.add('hidden');
closeModal.addEventListener('click', hideModal);
closeModalBtn.addEventListener('click', hideModal);
howItWorksModal.addEventListener('click', (e) => {
    if (e.target === howItWorksModal) hideModal();
});

// Drag & Drop Handlers
dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('border-blue-400', 'bg-blue-50');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('border-blue-400', 'bg-blue-50');
});

dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('border-blue-400', 'bg-blue-50');
    if (e.dataTransfer.files.length) {
        validateAndSetFile(e.dataTransfer.files[0]);
    }
});

dropZone.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length) {
        validateAndSetFile(e.target.files[0]);
    }
});

function validateAndSetFile(file) {
    if (file.type !== 'application/pdf' &&
        file.type !== 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' &&
        !file.type.startsWith('image/') &&
        !file.name.toLowerCase().endsWith('.docx')) {
        alert("Please upload a valid PDF, DOCX, or Image file (JPG/PNG).");
        return;
    }
    if (file.size > 5 * 1024 * 1024) { // 5MB
        alert("File size exceeds 5MB limit.");
        return;
    }
    selectedFile = file;

    // Update UI to show file selected
    dropZone.innerHTML = `
        <div class="p-4 bg-green-50 rounded-full shadow-sm mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <h3 class="text-lg font-semibold text-slate-800">${file.name}</h3>
        <p class="text-sm text-green-600 mt-1 font-medium">Ready to analyze</p>
        <button onclick="event.stopPropagation(); resetFile()" class="mt-4 text-xs text-slate-400 hover:text-red-500 underline">Remove file</button>
    `;

    checkFormState();
}

function resetFile() {
    selectedFile = null;
    fileInput.value = '';
    dropZone.innerHTML = `
        <div class="p-4 bg-white rounded-full shadow-sm mb-4 group-hover:scale-110 transition-transform">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-600"><path d="M4 14.899A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.5 8.242"></path><path d="M12 12v9"></path><path d="m16 16-4-4-4 4"></path></svg>
        </div>
        <h3 class="text-lg font-semibold text-slate-800">Drop your resume here</h3>
        <p class="text-sm text-slate-500 mt-1">or <span class="text-blue-600 font-medium underline">browse to upload</span></p>
        <p class="text-xs text-slate-400 mt-4">PDF, JPG, PNG files only, max 5MB</p>
    `;
    checkFormState();
}

jobRoleInput.addEventListener('input', () => {
    document.getElementById('role-error').classList.add('hidden');

    // Auto-select Technical Interviewer for IT roles
    const itKeywords = ['developer', 'engineer', 'data', 'software', 'backend', 'frontend', 'fullstack', 'cloud', 'devops', 'it '];
    const roleLower = jobRoleInput.value.toLowerCase();
    const techRadio = document.querySelector('input[value="Technical Interviewer"]');
    const hrRadio = document.querySelector('input[value="HR Recruiter"]');

    if (itKeywords.some(kw => roleLower.includes(kw))) {
        techRadio.checked = true;
    } else {
        hrRadio.checked = true;
    }

    // Update active visual state for chips
    document.querySelectorAll('.role-chip').forEach(chip => {
        if (chip.dataset.role.toLowerCase() === roleLower) {
            chip.classList.add('active');
        } else {
            chip.classList.remove('active');
        }
    });
});

// Quick Chips Logic
document.querySelectorAll('.role-chip').forEach(chip => {
    chip.addEventListener('click', () => {
        jobRoleInput.value = chip.dataset.role;
        jobRoleInput.dispatchEvent(new Event('input')); // trigger the input listener
    });
});


function checkFormState() {
    const hasText = pasteInput.value.trim().length > 0;
    // Only disable if no file and no text is selected. Role is checked on click.
    if (selectedFile || hasText) {
        analyzeBtn.disabled = false;
        analyzeBtn.classList.remove('bg-slate-300', 'cursor-not-allowed');
        analyzeBtn.classList.add('bg-blue-600', 'hover:bg-blue-700', 'shadow-lg', 'shadow-blue-200');
    } else {
        analyzeBtn.disabled = true;
        analyzeBtn.classList.add('bg-slate-300', 'cursor-not-allowed');
        analyzeBtn.classList.remove('bg-blue-600', 'hover:bg-blue-700', 'shadow-lg', 'shadow-blue-200');
    }
}

// API Call
analyzeBtn.addEventListener('click', async () => {
    const hasText = pasteInput.value.trim().length > 0;
    if (!selectedFile && !hasText) return;

    // Validate Role
    if (!jobRoleInput.value.trim()) {
        document.getElementById('role-error').classList.remove('hidden');
        jobRoleInput.focus();
        return;
    }

    // UI Transition
    uploadCard.classList.add('hidden');
    loadingView.classList.remove('hidden');

    const loaderHeading = document.getElementById('loader-heading');
    const loaderSub = document.getElementById('loader-sub');

    loaderHeading.style.opacity = '1';
    loaderSub.style.opacity = '1';
    loaderHeading.textContent = selectedFile && selectedFile.type.startsWith('image/')
        ? "Extracting text from image (OCR)..."
        : "Extracting text from document...";

    let isOcrStep = true;
    const loadingInterval = setInterval(() => {
        loaderHeading.style.opacity = '0';
        loaderSub.style.opacity = '0';

        setTimeout(() => {
            if (isOcrStep) {
                loaderHeading.textContent = "Analyzing your resume with Gemini AI...";
                loaderSub.textContent = "Checking bullets, skills, and ATS formatting...";
            } else {
                loaderHeading.textContent = "Finalizing HR-style feedback report...";
                loaderSub.textContent = "Generating missing skills and actionable rewrites...";
            }
            loaderHeading.style.opacity = '1';
            loaderSub.style.opacity = '1';
            isOcrStep = !isOcrStep;
        }, 300); // Wait for fade out
    }, 4000);

    const formData = new FormData();
    if (selectedFile) {
        formData.append('file', selectedFile);
    } else if (hasText) {
        formData.append('pastedText', pasteInput.value.trim());
    }
    formData.append('jobRole', jobRoleInput.value);

    // Add Job Description
    const jdInput = document.getElementById('job-description');
    if (jdInput && jdInput.value.trim()) {
        formData.append('jobDescription', jdInput.value.trim());
    }

    // Get selected radio
    const reviewer = document.querySelector('input[name="reviewer"]:checked').value;
    formData.append('reviewerType', reviewer);

    try {
        const response = await fetch('/api/resume/analyze', {
            method: 'POST',
            body: formData
        });

        clearInterval(loadingInterval);

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.error || 'Analysis failed');
        }

        const data = await response.json();
        renderResults(data, reviewer);

    } catch (error) {
        clearInterval(loadingInterval);
        console.error("Analysis Error:", error);

        // Show bad image message if explicitly an extraction error
        if (error.message.includes('text from this resume') || error.message.includes('upload a clearer')) {
            alert("Error: The text could not be extracted via OCR. Your image might be too blurry or low quality. Try an original PDF or type your resume directly.");
        } else {
            alert("Error: " + error.message);
        }

        loadingView.classList.add('hidden');
        uploadCard.classList.remove('hidden'); // Show upload form again
    }
});

const demoData = {
    score: 85,
    skillsScore: 90,
    experienceScore: 80,
    formattingScore: 75,
    clarityScore: 80,
    summary: "A robust candidate with strong proficiency in Java ecosystem, but lacks quantitative metrics. Overall, the candidate is a strong fit but should clarify their impact before interviewing.",
    topFixes: [
        "Add clear experience or projects relevant to Java Backend roles.",
        "Rewrite bullets to show impact, tools, and results.",
        "Add a focused summary aligned with the job."
    ],
    strengths: [
        "Strong grasp of Spring Boot architecture in core projects.",
        "Demonstrated experience with AWS / Cloud deployment and operations.",
        "Clear and consistent career progression over the past 3 years."
    ],
    missingSkills: ["[Must-have] Kafka - Add to Skills section", "[Nice-to-have] Redis", "[Must-have] System Design Patterns"],
    missingContactInfo: ["Mobile Number", "LinkedIn URL"],
    actionableEdits: [
        "Before: 'Worked on API' -> After: 'Designed and deployed REST API handling 50k+ daily requests with <100ms latency'.",
        "Move 'Skills' section to the top for better visibility."
    ],
    sectionFeedback: [
        {
            sectionName: "Experience",
            good: ["Consistent career progression", "Clear technical stack mentioned"],
            improvements: ["Lacks quantifiable metrics", "Needs to highlight leadership more"]
        },
        {
            sectionName: "Projects",
            good: ["Relevant enterprise projects"],
            improvements: ["Missing links to source code or live deployment"]
        }
    ],
    grammarIssues: ["None detected"]
};

function copyEdits() {
    const list = document.getElementById('actionable-list');
    const textToCopy = Array.from(list.querySelectorAll('span')).map(el => el.textContent).join('\n\n');
    navigator.clipboard.writeText(textToCopy).then(() => {
        alert("Actionable edits copied to clipboard!");
    });
}

// Demo Link Handler
document.getElementById('demo-link').addEventListener('click', (e) => {
    e.preventDefault();
    jobRoleInput.value = "Java Review Role (Demo)";
    uploadCard.classList.add('hidden');
    loadingView.classList.remove('hidden');

    // Simulate delay
    setTimeout(() => {
        renderResults(demoData, "HR Recruiter");
    }, 1500);
});

function renderResults(data, reviewer) {
    loadingView.classList.add('hidden');
    resultsView.classList.remove('hidden');

    // Update Meta & Summary Box
    document.getElementById('res-role').textContent = jobRoleInput.value;
    document.getElementById('summary-box-score').textContent = (data.score || 0) + '/100';
    document.getElementById('summary-box-role').textContent = jobRoleInput.value;
    document.getElementById('summary-box-perspective').textContent = reviewer || 'Unknown perspective';

    // Sub-scores (Horizontal Bars)
    const setScoreBar = (idSuffix, scoreVal) => {
        const val = scoreVal || 0;
        document.getElementById(`score-${idSuffix}-text`).textContent = `${val}/100`;
        setTimeout(() => {
            document.getElementById(`score-${idSuffix}-bar`).style.width = `${val}%`;
        }, 300); // delay slightly for animation
    };

    setScoreBar('skills', data.skillsScore);
    setScoreBar('experience', data.experienceScore);
    setScoreBar('formatting', data.formattingScore);
    setScoreBar('clarity', data.clarityScore);

    // Summary
    document.getElementById('res-summary').textContent = data.summary || "No summary available.";

    // Top Fixes
    const topFixesList = document.getElementById('top-fixes-list');
    if (data.topFixes && data.topFixes.length > 0) {
        topFixesList.innerHTML = data.topFixes.map(fix => `<li>${fix}</li>`).join('');
    } else {
        topFixesList.innerHTML = `<li class="text-secondary list-none">No top fixes suggested.</li>`;
    }

    // Strengths
    const strengthsList = document.getElementById('strengths-list');
    if (data.strengths && data.strengths.length > 0) {
        strengthsList.innerHTML = data.strengths.map(str => `<li>${str}</li>`).join('');
    } else {
        strengthsList.innerHTML = `<li class="text-secondary list-none">No specific strengths listed.</li>`;
    }

    // Actionable Edits
    const actionableList = document.getElementById('actionable-list');
    if (data.actionableEdits && data.actionableEdits.length > 0) {
        actionableList.innerHTML = data.actionableEdits.map(edit =>
            `<div style="display: flex; align-items: flex-start; gap: 0.75rem; padding: 1rem; background: rgba(88, 166, 255, 0.1); border: 1px solid rgba(88, 166, 255, 0.2); border-radius: 0.5rem;">
                <i data-lucide="edit" style="color: var(--primary); min-width: 18px; width: 18px; height: 18px; margin-top: 2px;"></i>
                <span style="color: var(--text-primary); font-size: 0.95rem; line-height: 1.5;">${edit}</span>
            </div>`
        ).join('');
    } else {
        actionableList.innerHTML = `<div style="padding: 1rem; color: var(--text-secondary);">No specific edits suggested.</div>`;
    }


    // Animate Score
    const scoreVal = document.getElementById('score-val');
    const scoreCircle = document.getElementById('score-circle');
    const scoreQuality = document.getElementById('score-quality');

    let currentScore = 0;
    const interval = setInterval(() => {
        currentScore++;
        scoreVal.textContent = currentScore;
        if (currentScore >= data.score) clearInterval(interval);
    }, 20);

    // Calculate Dash Offset (251.2 is circumference of r=40)
    const offset = 251.2 - (251.2 * data.score) / 100;
    setTimeout(() => {
        scoreCircle.style.strokeDashoffset = offset;
        // Color coding
        if (data.score >= 80) {
            scoreCircle.style.stroke = "#16a34a"; // Green
            scoreQuality.textContent = "Excellent Match";
            scoreQuality.classList.replace("text-amber-600", "text-green-600");
        } else if (data.score >= 60) {
            scoreCircle.style.stroke = "#ca8a04"; // Yellow
            scoreQuality.textContent = "Good / Average";
        } else {
            scoreCircle.style.stroke = "#dc2626"; // Red
            scoreQuality.textContent = "Needs Work";
            scoreQuality.classList.replace("text-amber-600", "text-red-600");
        }
    }, 100);

    // Missing Skills
    const missingContainer = document.getElementById('missing-skills-list');
    if (data.missingSkills && data.missingSkills.length > 0) {
        missingContainer.innerHTML = data.missingSkills.map(skill => {
            let label = skill;
            let tagClass = "bg-red-50 text-red-700 border-red-200";
            let tagText = "Needs Work";
            if (skill.toLowerCase().includes("[must-have]")) {
                label = skill.replace(/\[Must[-\s]?Have\]/i, "").trim();
                tagClass = "bg-red-50 text-red-700 border-red-200";
                tagText = "Must-have";
            } else if (skill.toLowerCase().includes("[nice-to-have]") || skill.toLowerCase().includes("[nice to have]")) {
                label = skill.replace(/\[Nice[-\s]to[-\s]Have\]/i, "").trim();
                tagClass = "bg-amber-50 text-amber-700 border-amber-200";
                tagText = "Nice to Have";
            }
            return `
                <li class="flex flex-col sm:flex-row sm:items-start gap-2 p-3 rounded-lg border border-slate-100 bg-slate-50/50">
                    <span class="inline-flex shrink-0 px-2 py-1 rounded text-[0.7rem] font-bold uppercase tracking-wider border ${tagClass}">
                        ${tagText}
                    </span>
                    <span class="text-sm text-slate-700 flex-1 leading-relaxed">${label}</span>
                </li>
            `;
        }).join('');
    } else {
        missingContainer.innerHTML = `<li class="px-3 py-2 bg-green-50 text-green-600 rounded-lg text-sm font-medium border border-green-100 list-none">No major skills missing!</li>`;
    }

    // Missing Contact Info
    const contactList = document.getElementById('contact-info-list');
    if (data.missingContactInfo && data.missingContactInfo.length > 0) {
        contactList.innerHTML = `<p class="mb-2 text-sm text-slate-400 list-none -ml-6 border-l-2 pl-4 border-amber-400">Add these to your resume so recruiters can contact you quickly:</p>` +
            data.missingContactInfo.map(item => `<li>${item}</li>`).join('');
    } else {
        contactList.innerHTML = `<li class="text-green-600 list-none flex items-center gap-2 -ml-6"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> All essential contact info is present!</li>`;
    }

    // Grammar Issues
    const grammarList = document.getElementById('grammar-list');
    if (data.grammarIssues && data.grammarIssues.length > 0) {
        grammarList.innerHTML = data.grammarIssues.map(issue => `<li>${issue}</li>`).join('');
    } else {
        grammarList.innerHTML = `<li class="text-green-600 list-none flex items-center gap-2"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg> No grammar issues detected.</li>`;
    }

    // Section by Section Feedback
    const sectionContainer = document.getElementById('section-feedback-list');
    if (data.sectionFeedback && data.sectionFeedback.length > 0) {
        sectionContainer.innerHTML = data.sectionFeedback.map(section => `
            <div class="bg-slate-50/50 border border-slate-100 rounded-lg p-4">
                <h4 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
                    <i data-lucide="layout-template" style="width: 16px; height: 16px; color: var(--primary);"></i>
                    ${section.sectionName}
                </h4>
                
                <div class="mb-3">
                    <p class="text-xs font-semibold text-green-600 uppercase tracking-wider mb-2">What's Good</p>
                    <ul class="list-disc pl-4 text-sm text-slate-700 space-y-1">
                        ${section.good && section.good.length > 0
                ? section.good.map(g => `<li>${g}</li>`).join('')
                : `<li class="text-slate-400 list-none">No specific positives noted.</li>`}
                    </ul>
                </div>
                
                <div>
                    <p class="text-xs font-semibold text-red-600 uppercase tracking-wider mb-2">What to Improve</p>
                    <ul class="list-disc pl-4 text-sm text-slate-700 space-y-1">
                        ${section.improvements && section.improvements.length > 0
                ? section.improvements.map(imp => `<li>${imp}</li>`).join('')
                : `<li class="text-slate-400 list-none">Looks good! No major issues.</li>`}
                    </ul>
                </div>
            </div>
        `).join('');
    } else {
        sectionContainer.innerHTML = `<div class="col-span-full p-4 text-secondary text-sm">No section-specific feedback generated.</div>`;
    }

    // Force Lucide icons to render in new content
    lucide.createIcons();
}
