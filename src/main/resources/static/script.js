const scriptURL = 'https://script.google.com/macros/s/AKfycbw-3jQHGtt0rrGb-Qr3Q56KJjjWPbymfQg1sBRLAlsRm_r-7VicCyH_dKPfvirLAE-E/exec';
let selectedFile = null;

/* ── Tab switching ── */
function switchTab(btn, id) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById('tab-file').style.display  = id === 'file'  ? '' : 'none';
  document.getElementById('tab-paste').style.display = id === 'paste' ? '' : 'none';
  if(id === 'paste') selectedFile = null; // Clear file when pasting
}

/* ── Drag & drop ── */
function dragOver(e) {
  e.preventDefault();
  document.getElementById('drop-zone').classList.add('drag-over');
}
function dragLeave(e) {
  document.getElementById('drop-zone').classList.remove('drag-over');
}
function handleDrop(e) {
  e.preventDefault();
  dragLeave(e);
  const file = e.dataTransfer.files[0];
  if (file) showFile(file);
}
function onFilePick(input) {
  if (input.files && input.files[0]) showFile(input.files[0]);
}
function showFile(file) {
  if (file.size > 5 * 1024 * 1024) {
      alert("File size exceeds 5MB limit.");
      return;
  }
  selectedFile = file;
  const dz = document.getElementById('drop-zone');
  dz.classList.add('has-file');
  const fc = document.getElementById('file-chosen');
  document.getElementById('file-name-text').textContent = file.name;
  fc.classList.add('show');
  markStep(1);
}

/* ── Role chips ── */
function setRole(r) {
  document.getElementById('role-input').value = r;
  onRoleType(); // Trigger role check
}
function onRoleType() {
  const inputEl = document.getElementById('role-input');
  const roleLower = inputEl.value.toLowerCase().trim();
  if (roleLower) markStep(2);
  
  // Auto switch perspective based on keywords
  const itKeywords = ['developer', 'engineer', 'data', 'software', 'backend', 'frontend', 'fullstack', 'cloud', 'devops', 'it '];
  if (itKeywords.some(kw => roleLower.includes(kw))) {
      selectPersp('tech');
  } else {
      selectPersp('hr');
  }
}

/* ── Perspective ── */
function selectPersp(p) {
  document.getElementById('persp-hr').classList.toggle('selected', p === 'hr');
  document.getElementById('persp-tech').classList.toggle('selected', p === 'tech');
  markStep(3);
}

/* ── Progress stepper ── */
function markStep(n) {
  for (let i = 1; i <= 4; i++) {
    const el = document.getElementById('s' + i);
    const lbl = document.getElementById('sl' + i);
    if (!el) continue;
    if (i < n) {
      el.classList.remove('active'); el.classList.add('done'); el.textContent = '✓';
      if (lbl) { lbl.classList.remove('active'); lbl.classList.add('done'); }
    } else if (i === n) {
      el.classList.add('active'); el.classList.remove('done');
      if (lbl) { lbl.classList.add('active'); lbl.classList.remove('done'); }
    }
  }
}

/* ── Star rating ── */
function rate(n) {
  document.getElementById('ratingInput').value = n;
  document.querySelectorAll('#stars .star').forEach((s, i) => {
    s.classList.toggle('lit', i < n);
  });
}

/* ── Copy section ── */
function copySection(id) {
  const el = document.getElementById(id);
  if (el) navigator.clipboard.writeText(el.innerText).then(() => {
    alert("Copied to clipboard!");
  }).catch(() => {});
}

// Demo link handling
document.getElementById('demo-link').addEventListener('click', (e) => {
    e.preventDefault();
    document.getElementById('role-input').value = "Java Developer Review (Demo)";
    executeAnalysis(true);
});

/* ── Analyze ── */
async function analyze() {
  const role = document.getElementById('role-input').value.trim();
  const pastedText = document.getElementById('paste-input').value.trim();
  
  if (!role) {
    document.getElementById('role-input').focus();
    document.getElementById('role-input').style.borderColor = 'rgba(239,68,68,0.6)';
    setTimeout(() => document.getElementById('role-input').style.borderColor = '', 2000);
    return;
  }
  
  if (!selectedFile && !pastedText) {
      alert("Please upload a resume file or paste your resume text.");
      return;
  }
  
  executeAnalysis(false);
}

async function executeAnalysis(isDemo) {
  const role = document.getElementById('role-input').value.trim();
  const pastedText = document.getElementById('paste-input').value.trim();
  const jdInput = document.getElementById('jd-input').value.trim();
  const reviewerStr = document.getElementById('persp-tech').classList.contains('selected') ? 'Technical Interviewer' : 'HR Recruiter';

  markStep(4);
  document.getElementById('form-section').style.display = 'none';
  document.getElementById('loading-section').style.display = 'block';
  document.getElementById('results-section').style.display = 'none';
  
  const loaderHeading = document.getElementById('loading-heading');
  const loaderSub = document.getElementById('loading-sub');

  loaderHeading.textContent = selectedFile && selectedFile.type.startsWith('image/')
      ? "Extracting text from image (OCR)…"
      : "Extracting text from document…";

  let isOcrStep = true;
  const loadingInterval = setInterval(() => {
      if (isOcrStep) {
          loaderHeading.textContent = "Analyzing your resume with Gemini AI…";
          loaderSub.textContent = "Checking bullets, skills, and ATS formatting…";
      } else {
          loaderHeading.textContent = "Finalizing " + (reviewerStr === 'Technical Interviewer' ? "technical" : "HR-style") + " feedback report…";
          loaderSub.textContent = "Generating missing skills and actionable rewrites…";
      }
      isOcrStep = !isOcrStep;
  }, 4000);

  if(isDemo) {
      clearInterval(loadingInterval);
      const demoData = {
          score: 85, skillsScore: 90, experienceScore: 80, formattingScore: 75, clarityScore: 80,
          summary: "A robust candidate with strong proficiency in Java ecosystem, but lacks quantitative metrics. Overall, the candidate is a strong fit but should clarify their impact before interviewing.",
          topFixes: ["Add clear experience or projects relevant to Java Backend roles.", "Rewrite bullets to show impact, tools, and results.", "Add a focused summary aligned with the job."],
          strengths: ["Strong grasp of Spring Boot architecture in core projects.", "Demonstrated experience with AWS / Cloud deployment and operations.", "Clear and consistent career progression over the past 3 years."],
          missingSkills: ["[Must-have] Kafka", "[Nice-to-have] Redis", "[Must-have] System Design Patterns"],
          missingContactInfo: ["Mobile Number", "LinkedIn URL"],
          actionableEdits: ["Before: 'Worked on API' -> After: 'Designed and deployed REST API handling 50k+ daily requests with <100ms latency'.", "Move 'Skills' section to the top for better visibility."],
          sectionFeedback: [{sectionName: "Experience", good: ["Consistent career progression", "Clear technical stack mentioned"], improvements: ["Lacks quantifiable metrics", "Needs to highlight leadership more"]}, {sectionName: "Projects", good: ["Relevant enterprise projects"], improvements: ["Missing links to source code or live deployment"]}],
          grammarIssues: ["None detected"]
      };
      setTimeout(() => {
          sessionStorage.setItem('resumeReport', JSON.stringify({ data: demoData, reviewer: reviewerStr, role: role }));
          window.location.href = 'report.html';
      }, 1500);
      return;
  }

  const formData = new FormData();
  if (selectedFile) formData.append('file', selectedFile);
  else if (pastedText) formData.append('pastedText', pastedText);
  
  formData.append('jobRole', role);
  if (jdInput) formData.append('jobDescription', jdInput);
  formData.append('reviewerType', reviewerStr);

  try {
      const response = await fetch('/api/resume/analyze', { method: 'POST', body: formData });
      clearInterval(loadingInterval);
      if (!response.ok) {
          const err = await response.json();
          throw new Error(err.error || 'Analysis failed');
      }
      const data = await response.json();
      sessionStorage.setItem('resumeReport', JSON.stringify({ data: data, reviewer: reviewerStr, role: role }));
      window.location.href = 'report.html';
  } catch (error) {
      clearInterval(loadingInterval);
      console.error("Analysis Error:", error);
      if (error.message.includes('text from this resume') || error.message.includes('upload a clearer')) {
          alert("Error: The text could not be extracted via OCR. Your image might be too blurry or low quality.");
      } else {
          alert("Error: " + error.message);
      }
      document.getElementById('loading-section').style.display = 'none';
      document.getElementById('form-section').style.display = 'block'; // Show upload form again
  }
}

function renderResults(data, reviewer, role) {
  document.getElementById('loading-section').style.display = 'none';
  document.getElementById('results-section').style.display = 'block';
  document.getElementById('results-meta').textContent = 'Role: ' + role + ' · Perspective: ' + reviewer;

  // Render Scores
  document.getElementById('val1').textContent = data.skillsScore || 0;
  document.getElementById('bar1').style.width = (data.skillsScore || 0) + '%';
  document.getElementById('val2').textContent = data.experienceScore || 0;
  document.getElementById('bar2').style.width = (data.experienceScore || 0) + '%';
  document.getElementById('val3').textContent = data.formattingScore || 0;
  document.getElementById('bar3').style.width = (data.formattingScore || 0) + '%';
  document.getElementById('val4').textContent = data.clarityScore || 0;
  document.getElementById('bar4').style.width = (data.clarityScore || 0) + '%';
  
  // Total Score Animation
  const totalScore = data.score || 0;
  const scoreRing = document.getElementById('score-ring');
  const scoreNumWrapper = document.getElementById('score-num');
  
  let cur = 0;
  const interval = setInterval(() => {
      cur++;
      scoreNumWrapper.textContent = cur;
      if (cur >= totalScore) clearInterval(interval);
  }, 20);
  
  setTimeout(() => {
      // 226 is the circumference of r=36
      const offset = 226 - (226 * totalScore) / 100;
      scoreRing.style.strokeDashoffset = offset;
      if (totalScore >= 80) scoreRing.style.stroke = "var(--green)";
      else if (totalScore >= 60) scoreRing.style.stroke = "var(--amber)";
      else scoreRing.style.stroke = "var(--red)";
  }, 100);

  // Executive Summary
  document.getElementById('exec-summary').textContent = data.summary || "No summary available.";

  // Top Fixes - Enhanced with special class
  const fixesContainer = document.getElementById('fixes-content').parentElement;
  fixesContainer.classList.add('special-fix');
  const fixesHtml = data.topFixes && data.topFixes.length > 0 
      ? data.topFixes.map((f, i) => `<div class="fix-item"><div class="fix-num">${i+1}</div><div class="fix-text">${f}</div></div>`).join('')
      : "<div class='rs-content' style='color:var(--muted)'>No top fixes suggested.</div>";
  document.getElementById('fixes-content').innerHTML = fixesHtml;

  // Strengths - Enhanced with special class
  const strengthsContainer = document.getElementById('strengths-list').parentElement;
  strengthsContainer.classList.add('special-strength');
  const strengthsHtml = data.strengths && data.strengths.length > 0
      ? data.strengths.map(s => `<span class="tag tag-green">${s}</span>`).join('')
      : "<span class='rs-content' style='color:var(--muted)'>No specific strengths listed.</span>";
  document.getElementById('strengths-list').innerHTML = strengthsHtml;

  // Missing Skills - Enhanced with special class
  const missingContainer = document.getElementById('missing-list').parentElement;
  missingContainer.classList.add('special-missing');
  const missingHtml = data.missingSkills && data.missingSkills.length > 0
      ? data.missingSkills.map(s => {
          let tClass = "tag-red";
          let sText = s;
          if(s.toLowerCase().includes("nice-to-have")) { tClass = "tag-amber"; sText = s.replace(/\[Nice[-\s]to[-\s]Have\]/i, "").trim(); }
          else if(s.toLowerCase().includes("must-have")) { sText = s.replace(/\[Must[-\s]?Have\]/i, "").trim(); }
          return `<span class="tag ${tClass}">${sText}</span>`;
      }).join('')
      : "<span class='rs-content' style='color:var(--green)'>No major skills missing!</span>";
  document.getElementById('missing-list').innerHTML = missingHtml;

  // Actionable Edits - Enhanced with before/after format
  const editsHtml = data.actionableEdits && data.actionableEdits.length > 0
      ? data.actionableEdits.map(e => {
          const parts = e.split(/->|→/);
          if (parts.length >= 2) {
              return `<div class="action-edit">
                  <strong>Rewrite Suggestion</strong>
                  <div class="before-after">
                      <div class="before">Before: ${parts[0].replace(/^Before:\s*/i, '').trim()}</div>
                      <div class="after">After: ${parts.slice(1).join('→').replace(/^After:\s*/i, '').trim()}</div>
                  </div>
              </div>`;
          }
          return `<div class="action-edit"><div class="rs-content">${e}</div></div>`;
      }).join('')
      : "<div class='rs-content' style='color:var(--muted)'>No specific rewrites suggested.</div>";
  document.getElementById('edits-content').innerHTML = editsHtml;

  // Section Feedback - Enhanced
  const sectionsHtml = data.sectionFeedback && data.sectionFeedback.length > 0
      ? data.sectionFeedback.map(sf => `
          <div class="section-item">
              <div class="section-header">
                  <span class="section-name">${sf.sectionName}</span>
                  <span class="good-tag">Good</span>
              </div>
              <ul>${(sf.good || []).map(g=>`<li>${g}</li>`).join('') || "<li>No specific strengths noted</li>"}</ul>
              <div class="section-header" style="margin-top:12px;">
                  <span class="section-name">Improvements</span>
                  <span class="improve-tag">Fix</span>
              </div>
              <ul>${(sf.improvements || []).map(g=>`<li>${g}</li>`).join('') || "<li>No specific improvements noted</li>"}</ul>
          </div>
      `).join('')
      : "<div class='rs-content' style='color:var(--muted)'>No detailed section feedback generated.</div>";
  document.getElementById('section-feedback').innerHTML = sectionsHtml;

  // Grammar & Buzzwords
  const grammarHtml = data.grammarIssues && data.grammarIssues.length > 0
      ? `<div class="rs-content" style="padding:12px; background:var(--bg); border-radius:var(--r-md); border-left:3px solid var(--amber);">
          <ul style="padding-left:16px; color:var(--muted); font-size:13px; margin:0;">${data.grammarIssues.map(gi => `<li>${gi}</li>`).join('')}</ul>
         </div>`
      : "<div class='rs-content' style='color:var(--green)'>✓ No grammar issues or buzzwords detected.</div>";
  document.getElementById('grammar-content').innerHTML = grammarHtml;

  // Missing Contact
  const contactHtml = data.missingContactInfo && data.missingContactInfo.length > 0
      ? data.missingContactInfo.map(c => `<span class="tag tag-blue">${c}</span>`).join('')
      : "<span class='rs-content' style='color:var(--green)'>✓ All essential contact info is present!</span>";
  document.getElementById('contact-list').innerHTML = contactHtml;
}

/* ── Feedback Form ── */
function submitFeedback(e) {
  e.preventDefault();
  const fMsg = document.getElementById('fbMsg');
  const fSubmit = document.getElementById('fbSubmit');
  
  fSubmit.disabled = true;
  fSubmit.textContent = 'Submitting...';

  const formData = new FormData();
  formData.append('name', document.getElementById('fbName').value);
  formData.append('rating', document.getElementById('ratingInput').value || "5");
  formData.append('message', document.getElementById('fbMessage').value);
  
  const searchParams = new URLSearchParams();
  for (const pair of formData.entries()) {
      searchParams.append(pair[0], pair[1]);
  }

  fetch(scriptURL, {
      method: 'POST',
      body: searchParams,
      mode: 'no-cors'
  }).then(() => {
      fMsg.style.display = 'block';
      fMsg.style.color = 'var(--green)';
      fMsg.textContent = 'Thank you! Your feedback is submitted.';
      e.target.reset();
      rate(0); // reset stars
      fSubmit.disabled = false;
      fSubmit.textContent = 'Submit feedback';
      setTimeout(() => fMsg.style.display = 'none', 5000);
  }).catch((err) => {
      fMsg.style.display = 'block';
      fMsg.style.color = 'var(--red)';
      fMsg.textContent = 'There was an error submitting. Try again.';
      fSubmit.disabled = false;
      fSubmit.textContent = 'Submit feedback';
  });
}

/* ── Reset ── */
function resetForm() {
  document.getElementById('results-section').style.display = 'none';
  document.getElementById('form-section').style.display = 'block';
  document.getElementById('role-input').value = '';
  document.getElementById('drop-zone').classList.remove('has-file');
  document.getElementById('file-chosen').classList.remove('show');
  document.getElementById('fileInput').value = '';
  document.getElementById('paste-input').value = '';
  selectedFile = null;
  
  document.getElementById('score-num').textContent = "0";
  document.getElementById('score-ring').style.strokeDashoffset = "226";

  for (let i = 1; i <= 4; i++) {
    const el = document.getElementById('s' + i);
    const lbl = document.getElementById('sl' + i);
    if (!el) continue;
    el.classList.remove('done','active');
    el.textContent = i;
    if (lbl) { lbl.classList.remove('done','active'); }
  }
  document.getElementById('s1').classList.add('active');
  document.getElementById('sl1') && document.getElementById('sl1').classList.add('active');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
