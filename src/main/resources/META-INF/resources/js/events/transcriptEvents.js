// transcriptEvents.js - encapsulate transcript page logic
import { Toast } from '../components/toast.js';
import { Auth } from '../auth/Auth.js';
import { Store } from '../state/store.js';
import { handleAPIError } from '../services/errorHandler.js';

export function initTranscript() {
  let allStudents = [];
  let loggedInUser = null;
  let cachedDashboardData = null;

  // Dynamically import API service
  import('../services/apiService.js').then(({ apiService: API }) => {
    // Local showToast delegation to centralized Toast module
    function showToast(message, type = 'success') {
      Toast.show(message, type);
    }

    // Logout Helper
    function logout() {
      Auth.logout();
    }

    // Initialize Page
    async function init() {
      console.log('Initializing official transcript...');
      if (!Auth.isAuthenticated()) {
        Auth.logout();
        return;
      }
      loggedInUser = JSON.parse(userJson);

      // Set static date and verification code
      const today = new Date();
      document.getElementById('current-date').textContent = today.toLocaleDateString('en-US', {
        year: 'numeric', month: 'long', day: 'numeric'
      });
      document.getElementById('doc-id').textContent = 'GP-' + Math.random().toString(36).substr(2, 9).toUpperCase();

      // Register term selector listener
      const termSelect = document.getElementById('term-select');
      if (termSelect) termSelect.addEventListener('change', handleTermSelectChange);

      // Role‑gated landing gate
      if (loggedInUser.role === 'STUDENT') {
        document.getElementById('student-selector-card').style.display = 'none';
        document.getElementById('placeholder-text').textContent = 'Fetching your official registry record, please hold...';
        await loadStudentTranscript(loggedInUser.userId || loggedInUser.id, loggedInUser.username);
      } else {
        document.getElementById('student-selector-card').style.display = 'flex';
        document.getElementById('transcript-placeholder').style.display = 'block';
        document.getElementById('transcript-card').style.display = 'none';
        document.getElementById('actions-bar').style.display = 'none';
        const termSelectorCard = document.getElementById('term-selector-card');
        if (termSelectorCard) termSelectorCard.style.display = 'none';
        await populateStudentList();
      }
      if (window.hidePageLoader) window.hidePageLoader();
    }

    // Populate Student dropdown for Admin/Faculty
    async function populateStudentList() {
      try {
        allStudents = await API.getStudents();
        const select = document.getElementById('student-select');
        select.innerHTML = '<option value="">Select a student...</option>';
        allStudents.forEach(s => {
          const opt = document.createElement('option');
          opt.value = s.id;
          opt.textContent = `${s.name} (${s.studentId})`;
          select.appendChild(opt);
        });
        select.addEventListener('change', handleStudentSelectChange);
      } catch (err) {
        console.error('Error loading registry list:', err);
        showToast('Failed to load students from registry.', 'error');
      }
    }

    // Selector Trigger for Admin/Faculty
    async function handleStudentSelectChange(e) {
      const studentId = e.target.value;
      if (!studentId) {
        document.getElementById('transcript-placeholder').style.display = 'block';
        document.getElementById('transcript-card').style.display = 'none';
        document.getElementById('actions-bar').style.display = 'none';
        const termSelectorCard = document.getElementById('term-selector-card');
        if (termSelectorCard) termSelectorCard.style.display = 'none';
        return;
      }
      const student = allStudents.find(s => s.id == studentId);
      const studentName = student ? student.name : 'Unknown Registry Profile';
      await loadStudentTranscript(studentId, studentName);
    }

    // Core Loader for a student transcript
    async function loadStudentTranscript(studentId, defaultName) {
      try {
        let studentName = defaultName;
        let rollNumber = '-';
        let branchName = 'Computer Science';
        try {
          const registryStudent = await API.getStudent(studentId);
          if (registryStudent) {
            studentName = registryStudent.name;
            rollNumber = registryStudent.studentId;
            branchName = registryStudent.branch || 'Computer Science';
          }
        } catch (regErr) {
          console.warn('Student details not in main registry list, using local username', regErr);
          rollNumber = 'REG-' + String(studentId).padStart(4, '0');
        }
        document.getElementById('student-name').textContent = studentName;
        document.getElementById('student-branch').textContent = branchName;
        document.getElementById('student-roll').textContent = rollNumber;

        const dashboardData = await API.getStudentDashboard(studentId);
        if (!dashboardData || !dashboardData.semesterResults || dashboardData.semesterResults.length === 0) {
          document.getElementById('transcript-placeholder').style.display = 'block';
          document.getElementById('placeholder-text').textContent = 'No academic records found for this student profile.';
          document.getElementById('transcript-card').style.display = 'none';
          document.getElementById('actions-bar').style.display = 'none';
          const termSelectorCard = document.getElementById('term-selector-card');
          if (termSelectorCard) termSelectorCard.style.display = 'none';
          showToast('This student has no active enrollment records.', 'error');
          return;
        }
        cachedDashboardData = dashboardData;
        const termSelect = document.getElementById('term-select');
        if (termSelect) {
          termSelect.innerHTML = '<option value="all">Official Cumulative Transcript (All Semesters)</option>';
          dashboardData.semesterResults.forEach(sem => {
            const opt = document.createElement('option');
            opt.value = sem.semesterNumber;
            opt.textContent = `Semester ${sem.semesterNumber} Grade Sheet`;
            termSelect.appendChild(opt);
          });
          termSelect.value = 'all';
        }
        // Reset static labels
        const docTitle = document.getElementById('document-title');
        const cgpaLabel = document.getElementById('cgpa-label');
        const creditsLabel = document.getElementById('credits-label');
        const coursesLabel = document.getElementById('courses-label');
        const standingLabel = document.getElementById('standing-label');
        const standingSub = document.getElementById('standing-sub');
        if (docTitle) docTitle.textContent = 'Official Transcript of Academic Record';
        if (cgpaLabel) cgpaLabel.textContent = 'Cumulative GPA';
        if (creditsLabel) creditsLabel.textContent = 'Total Credits';
        if (coursesLabel) coursesLabel.textContent = 'Completed Courses';
        if (standingLabel) standingLabel.textContent = 'Standing';
        if (standingSub) standingSub.textContent = 'Official Degree Range';

        const summaryBox = document.getElementById('attestation-summary-box');
        if (summaryBox) summaryBox.style.gridTemplateColumns = 'repeat(4, 1fr)';
        const cgpaExtraBlock = document.getElementById('block-cgpa-extra');
        if (cgpaExtraBlock) cgpaExtraBlock.style.display = 'none';

        renderLedger(dashboardData.semesterResults);
        const finalCgpa = dashboardData.cgpa || 0;
        document.getElementById('final-cgpa').textContent = finalCgpa.toFixed(2);
        document.getElementById('final-credits').textContent = dashboardData.totalCredits || 0;
        document.getElementById('final-courses').textContent = dashboardData.totalCourses || 0;
        document.getElementById('classification').textContent = getClassification(finalCgpa);

        document.getElementById('transcript-placeholder').style.display = 'none';
        document.getElementById('transcript-card').style.display = 'block';
        document.getElementById('actions-bar').style.display = 'flex';
        const termSelectorCard = document.getElementById('term-selector-card');
        if (termSelectorCard) termSelectorCard.style.display = 'flex';
        showToast('Official transcript generated successfully!', 'success');
      } catch (err) {
        console.error('Error fetching academic transcript:', err);
        showToast('Failed to compile transcript from ledger.', 'error');
        document.getElementById('transcript-placeholder').style.display = 'block';
        document.getElementById('placeholder-text').textContent = 'Compilation failed. Please try again.';
        document.getElementById('transcript-card').style.display = 'none';
        document.getElementById('actions-bar').style.display = 'none';
        const termSelectorCard = document.getElementById('term-selector-card');
        if (termSelectorCard) termSelectorCard.style.display = 'none';
      }
    }

    // Handle term / marks card select change
    function handleTermSelectChange(e) {
      if (!cachedDashboardData) return;
      const val = e.target.value;
      const docTitle = document.getElementById('document-title');
      const cgpaLabel = document.getElementById('cgpa-label');
      const creditsLabel = document.getElementById('credits-label');
      const coursesLabel = document.getElementById('courses-label');
      const standingLabel = document.getElementById('standing-label');
      const standingSub = document.getElementById('standing-sub');
      const summaryBox = document.getElementById('attestation-summary-box');
      const cgpaExtraBlock = document.getElementById('block-cgpa-extra');

      if (val === 'all') {
        renderLedger(cachedDashboardData.semesterResults);
        if (docTitle) docTitle.textContent = 'Official Transcript of Academic Record';
        if (cgpaLabel) cgpaLabel.textContent = 'Cumulative GPA';
        if (creditsLabel) creditsLabel.textContent = 'Total Credits';
        if (coursesLabel) coursesLabel.textContent = 'Completed Courses';
        if (standingLabel) standingLabel.textContent = 'Standing';
        if (standingSub) standingSub.textContent = 'Official Degree Range';
        if (summaryBox) summaryBox.style.gridTemplateColumns = 'repeat(4, 1fr)';
        if (cgpaExtraBlock) cgpaExtraBlock.style.display = 'none';
        const cgpa = cachedDashboardData.cgpa || 0;
        document.getElementById('final-cgpa').textContent = cgpa.toFixed(2);
        document.getElementById('final-credits').textContent = cachedDashboardData.totalCredits || 0;
        document.getElementById('final-courses').textContent = cachedDashboardData.totalCourses || 0;
        document.getElementById('classification').textContent = getClassification(cgpa);
      } else {
        const semNum = parseInt(val, 10);
        const sem = cachedDashboardData.semesterResults.find(s => s.semesterNumber === semNum);
        if (!sem) return;
        renderLedger([sem]);
        if (docTitle) docTitle.textContent = `Official Grade Sheet | Semester ${semNum}`;
        if (cgpaLabel) cgpaLabel.textContent = 'Semester SGPA';
        if (creditsLabel) creditsLabel.textContent = 'Semester Credits';
        if (coursesLabel) coursesLabel.textContent = 'Semester Courses';
        if (standingLabel) standingLabel.textContent = 'Term Standing';
        if (standingSub) standingSub.textContent = 'Academic Division';
        if (summaryBox) summaryBox.style.gridTemplateColumns = 'repeat(5, 1fr)';
        if (cgpaExtraBlock) cgpaExtraBlock.style.display = 'flex';
        let accumPoints = 0, accumCredits = 0;
        cachedDashboardData.semesterResults.forEach(s => {
          if (s.semesterNumber <= semNum) {
            accumPoints += s.totalPoints || 0;
            accumCredits += s.totalCredits || 0;
          }
        });
        const cgpaUpToSem = accumCredits > 0 ? (accumPoints / accumCredits) : 0;
        document.getElementById('final-cgpa').textContent = sem.sgpa.toFixed(2);
        document.getElementById('final-cgpa-extra').textContent = cgpaUpToSem.toFixed(2);
        document.getElementById('final-cgpa-extra').parentNode.querySelector('.summary-block-sub').textContent = `Sem 1 - ${semNum} Avg`;
        document.getElementById('final-credits').textContent = sem.totalCredits || 0;
        document.getElementById('final-courses').textContent = sem.courses ? sem.courses.length : 0;
        document.getElementById('classification').textContent = getClassification(sem.sgpa);
      }
    }

    // Render ledger (dynamic semesters)
    function renderLedger(semesterResults) {
      const container = document.getElementById('results-container');
      container.innerHTML = '';
      semesterResults.forEach(sem => {
        let rowHTML = '';
        sem.courses.forEach(c => {
          const gradeClass = c.grade.replace('+', '-plus');
          rowHTML += `
            <tr>
              <td class="code">${c.courseCode}</td>
              <td class="course-name">${c.courseName}</td>
              <td class="centered" style="font-weight: 600;">${c.credits}</td>
              <td class="centered" style="font-weight: 500;">${c.creditPoints}</td>
              <td class="centered"><span class="grade-badge-custom ${gradeClass}">${c.grade}</span></td>
            </tr>
          `;
        });
        container.innerHTML += `
          <div class="semester-block">
            <div class="semester-block-header">
              <h4 class="semester-block-title">Semester ${sem.semesterNumber} Ledger</h4>
              <div class="semester-block-gpa">SGPA: ${sem.sgpa.toFixed(2)}</div>
            </div>
            <table class="ledger-table">
              <thead>
                <tr>
                  <th style="width: 15%;">Course Code</th>
                  <th style="width: 50%;">Course Title</th>
                  <th style="width: 12%; text-align: center;">Credits</th>
                  <th style="width: 12%; text-align: center;">Points</th>
                  <th style="width: 11%; text-align: center;">Grade</th>
                </tr>
              </thead>
              <tbody>
                ${rowHTML}
              </tbody>
            </table>
          </div>
        `;
      });
    }

    // Classification logic
    function getClassification(cgpa) {
      if (cgpa >= 9.0) return 'First Class with Distinction';
      if (cgpa >= 8.0) return 'First Class';
      if (cgpa >= 7.0) return 'Second Class (Upper Division)';
      if (cgpa >= 6.0) return 'Second Class (Lower Division)';
      if (cgpa >= 5.0) return 'Third Class';
      if (cgpa >= 4.0) return 'Pass';
      return 'Academic Warning';
    }

    // Expose needed globals for inline handlers
    window.logout = logout;
    window.handleStudentSelectChange = handleStudentSelectChange;
    window.handleTermSelectChange = handleTermSelectChange;

    // Kick off init
    init();
  });
}
