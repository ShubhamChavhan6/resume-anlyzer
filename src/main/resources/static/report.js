/* ═══════════════════════════════════════════════════════
   ResumeAI – Report Page Script
   Reads analysis data from sessionStorage and renders
   ═══════════════════════════════════════════════════════ */

const scriptURL = 'https://script.google.com/macros/s/AKfycbw-3jQHGtt0rrGb-Qr3Q56KJjjWPbymfQg1sBRLAlsRm_r-7VicCyH_dKPfvirLAE-E/exec';

/* ── Load data on page open ── */
document.addEventListener('DOMContentLoaded', () => {
  const raw = sessionStorage.getItem('resumeReport');
  if (!raw) {
    showNoData();
    return;
  }

  try {
    const report = JSON.parse(raw);
    renderReport(report.data, report.reviewer, report.role);
  } catch (e) {
    console.error('Failed to parse report data:', e);
    showNoData();
  }
});

function showNoData() {
  const wrap = document.querySelector('.report-wrap');
  // Hide everything except nav & footer
  document.getElementById('report-header').style.display = 'none';
  document.getElementById('score-dashboard').style.display = 'none';
  document.querySelector('.report-grid').style.display = 'none';
  document.querySelector('.card-feedback').style.display = 'none';
  document.querySelector('.bottom-cta').style.display = 'none';

  const noData = document.createElement('div');
  noData.className = 'no-data';
  noData.innerHTML = `
    <div class="no-data-icon">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--blue)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
        <line x1="12" y1="18" x2="12" y2="12"/>
        <polyline points="9 15 12 12 15 15"/>
      </svg>
    </div>
    <h3>No analysis report found</h3>
    <p>Upload your resume first to generate an analysis report.</p>
    <a href="index.html">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <line x1="12" y1="5" x2="12" y2="19"/><polyline points="5 12 12 5 19 12"/>
      </svg>
      Upload Resume
    </a>
  `;

  // Insert after the nav
  const nav = document.querySelector('.report-nav');
  nav.insertAdjacentElement('afterend', noData);
}

/* ── Render report data ── */
function renderReport(data, reviewer, role) {
  // Meta
  document.getElementById('meta-role').textContent = role || 'General';
  document.getElementById('meta-reviewer').textContent = reviewer || 'HR Recruiter';
  document.getElementById('meta-time').textContent = new Date().toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  });

  // Score ring
  const totalScore = data.score || 0;
  const ring = document.getElementById('score-ring');
  const numEl = document.getElementById('score-num');
  const verdict = document.getElementById('score-verdict');

  // Animate the number counting up
  let cur = 0;
  const countInterval = setInterval(() => {
    cur++;
    numEl.textContent = cur;
    if (cur >= totalScore) clearInterval(countInterval);
  }, 18);

  // Animate ring fill
  setTimeout(() => {
    const circumference = 2 * Math.PI * 50; // r=50
    const offset = circumference - (circumference * totalScore) / 100;
    ring.style.strokeDashoffset = offset;

    // Change color based on score
    if (totalScore >= 80) {
      ring.style.stroke = 'var(--green)';
      verdict.textContent = '🟢 Excellent — Your resume is strong!';
      verdict.style.color = 'var(--green)';
    } else if (totalScore >= 60) {
      ring.style.stroke = 'var(--amber)';
      verdict.textContent = '🟡 Good — A few improvements needed';
      verdict.style.color = 'var(--amber)';
    } else {
      ring.style.stroke = 'var(--red)';
      verdict.textContent = '🔴 Needs work — Major improvements required';
      verdict.style.color = 'var(--red)';
    }
  }, 200);

  // Sub-scores
  animateBar('bar-skills', 'val-skills', data.skillsScore);
  animateBar('bar-exp', 'val-exp', data.experienceScore);
  animateBar('bar-fmt', 'val-fmt', data.formattingScore);
  animateBar('bar-clarity', 'val-clarity', data.clarityScore);

  // Executive Summary
  document.getElementById('exec-summary').textContent = data.summary || 'No summary available.';

  // Top Fixes
  const fixesEl = document.getElementById('fixes-content');
  if (data.topFixes && data.topFixes.length > 0) {
    fixesEl.innerHTML = data.topFixes.map((f, i) => `
      <div class="fix-row">
        <div class="fix-number">${i + 1}</div>
        <div class="fix-body">${escapeHtml(f)}</div>
      </div>
    `).join('');
  } else {
    fixesEl.innerHTML = '<div class="grammar-ok">✓ No critical fixes needed!</div>';
  }

  // Strengths
  const strengthsEl = document.getElementById('strengths-list');
  if (data.strengths && data.strengths.length > 0) {
    strengthsEl.innerHTML = data.strengths.map(s => `
      <div class="strength-item">
        <div class="strength-icon">✓</div>
        <div class="strength-text">${escapeHtml(s)}</div>
      </div>
    `).join('');
  } else {
    strengthsEl.innerHTML = '<div style="color:var(--muted); font-size:13px;">No specific strengths noted.</div>';
  }

  // Missing Skills
  const missingEl = document.getElementById('missing-list');
  if (data.missingSkills && data.missingSkills.length > 0) {
    missingEl.innerHTML = data.missingSkills.map(s => {
      let tagClass = 'tag-red';
      let badge = '<span class="tag-must">must</span>';
      let sText = s;

      if (s.toLowerCase().includes('nice-to-have')) {
        tagClass = 'tag-amber';
        badge = '<span class="tag-nice">nice</span>';
        sText = s.replace(/\[Nice[-\s]to[-\s]Have\]/i, '').trim();
      } else if (s.toLowerCase().includes('must-have')) {
        sText = s.replace(/\[Must[-\s]?Have\]/i, '').trim();
      }

      return `<span class="tag ${tagClass}">${badge} ${escapeHtml(sText)}</span>`;
    }).join('');
  } else {
    missingEl.innerHTML = '<div class="grammar-ok">✓ No major skills missing!</div>';
  }

  // Actionable Edits
  const editsEl = document.getElementById('edits-content');
  if (data.actionableEdits && data.actionableEdits.length > 0) {
    editsEl.innerHTML = data.actionableEdits.map(e => {
      const parts = e.split(/->|→/);
      if (parts.length >= 2) {
        const before = parts[0].replace(/^Before:\s*/i, '').trim();
        const after = parts.slice(1).join('→').replace(/^After:\s*/i, '').trim();
        return `
          <div class="edit-block">
            <div class="edit-label">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
              Rewrite suggestion
            </div>
            <div class="edit-before">${escapeHtml(before)}</div>
            <div class="edit-after">${escapeHtml(after)}</div>
          </div>`;
      }
      return `<div class="edit-block"><div class="edit-plain">${escapeHtml(e)}</div></div>`;
    }).join('');
  } else {
    editsEl.innerHTML = '<div style="color:var(--muted); font-size:13px;">No specific rewrites suggested.</div>';
  }

  // Section Feedback
  const sectionsEl = document.getElementById('section-feedback');
  if (data.sectionFeedback && data.sectionFeedback.length > 0) {
    sectionsEl.innerHTML = data.sectionFeedback.map(sf => `
      <div class="section-block">
        <div class="section-block-header">
          <div class="section-block-name">${escapeHtml(sf.sectionName)}</div>
        </div>
        <div class="section-block-body">
          <div class="feedback-group">
            <div class="feedback-group-label label-good">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
              What's good
            </div>
            <ul>${(sf.good || []).map(g => `<li>${escapeHtml(g)}</li>`).join('') || '<li>No specific strengths noted</li>'}</ul>
          </div>
          <div class="feedback-group">
            <div class="feedback-group-label label-improve">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="12" y1="2" x2="12" y2="16"/><polyline points="6 10 12 16 18 10"/></svg>
              Needs improvement
            </div>
            <ul>${(sf.improvements || []).map(g => `<li>${escapeHtml(g)}</li>`).join('') || '<li>No specific improvements noted</li>'}</ul>
          </div>
        </div>
      </div>
    `).join('');
  } else {
    sectionsEl.innerHTML = '<div style="color:var(--muted); font-size:13px;">No detailed section feedback generated.</div>';
  }

  // Grammar
  const grammarEl = document.getElementById('grammar-content');
  if (data.grammarIssues && data.grammarIssues.length > 0) {
    const hasIssues = data.grammarIssues.some(gi => gi.toLowerCase() !== 'none detected' && gi.toLowerCase() !== 'none');
    if (hasIssues) {
      grammarEl.innerHTML = `<div class="grammar-list">
        ${data.grammarIssues.map(gi => `<div class="grammar-item">${escapeHtml(gi)}</div>`).join('')}
      </div>`;
    } else {
      grammarEl.innerHTML = '<div class="grammar-ok">✓ No grammar issues or buzzwords detected.</div>';
    }
  } else {
    grammarEl.innerHTML = '<div class="grammar-ok">✓ No grammar issues or buzzwords detected.</div>';
  }

  // Missing Contact
  const contactEl = document.getElementById('contact-list');
  if (data.missingContactInfo && data.missingContactInfo.length > 0) {
    contactEl.innerHTML = data.missingContactInfo.map(c =>
      `<span class="tag tag-blue">${escapeHtml(c)}</span>`
    ).join('');
  } else {
    contactEl.innerHTML = '<div class="grammar-ok">✓ All essential contact info is present!</div>';
  }
}

/* ── Helpers ── */
function animateBar(barId, valId, score) {
  const val = score || 0;
  setTimeout(() => {
    document.getElementById(barId).style.width = val + '%';
    // Animate value counting
    let cur = 0;
    const interval = setInterval(() => {
      cur++;
      document.getElementById(valId).textContent = cur;
      if (cur >= val) clearInterval(interval);
    }, 20);
  }, 400);
}

function escapeHtml(unsafe) {
  if (!unsafe) return '';
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function copySection(id) {
  const el = document.getElementById(id);
  if (el) {
    navigator.clipboard.writeText(el.innerText).then(() => {
      // Flash the copy button
      const btn = el.closest('.report-card').querySelector('.copy-btn');
      if (btn) {
        const orig = btn.innerHTML;
        btn.innerHTML = '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> Copied!';
        btn.style.borderColor = 'var(--green)';
        btn.style.color = 'var(--green)';
        setTimeout(() => {
          btn.innerHTML = orig;
          btn.style.borderColor = '';
          btn.style.color = '';
        }, 2000);
      }
    }).catch(() => {});
  }
}

/* ── Star Rating ── */
function rate(n) {
  document.getElementById('ratingInput').value = n;
  document.querySelectorAll('#stars .star').forEach((s, i) => {
    s.classList.toggle('lit', i < n);
  });
}

/* ── Feedback Submission ── */
function submitFeedback() {
  const fMsg = document.getElementById('fbMsg');
  const fSubmit = document.getElementById('fbSubmit');

  fSubmit.disabled = true;
  fSubmit.textContent = 'Submitting...';

  const searchParams = new URLSearchParams();
  searchParams.append('name', document.getElementById('fbName').value);
  searchParams.append('rating', document.getElementById('ratingInput').value || '5');
  searchParams.append('message', document.getElementById('fbMessage').value);

  fetch(scriptURL, {
    method: 'POST',
    body: searchParams,
    mode: 'no-cors'
  }).then(() => {
    fMsg.style.display = 'block';
    fMsg.style.color = 'var(--green)';
    fMsg.textContent = 'Thank you! Your feedback is submitted.';
    document.getElementById('fbName').value = '';
    document.getElementById('fbMessage').value = '';
    rate(0);
    fSubmit.disabled = false;
    fSubmit.textContent = 'Submit feedback';
    setTimeout(() => fMsg.style.display = 'none', 5000);
  }).catch(() => {
    fMsg.style.display = 'block';
    fMsg.style.color = 'var(--red)';
    fMsg.textContent = 'Error submitting. Try again.';
    fSubmit.disabled = false;
    fSubmit.textContent = 'Submit feedback';
  });
}

/* ── Download Report as Print-PDF ── */
function downloadReport() {
  window.print();
}
