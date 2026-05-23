// Faculty Events Module
// This module encapsulates all faculty page logic previously in faculty.js
// Export a single init function to be called by the thin bootstrap script.

export function initFaculty() {
  let pendingChanges = {}; // Maps enrollmentId -> newGrade string
  let originalEnrollments = [];
  let activeFilterType = 'course'; // course, semester, student

  // Check if token exists
  if (!localStorage.getItem('token')) {
    window.location.href = '/';
    return;
  }

  // Import API service dynamically (avoid top-level import to keep this module pure)
  // Assuming apiService.js is an ES module exporting { apiService as API }
  // We'll import it here.
  import('../services/apiService.js').then(({ apiService: API }) => {
    // Utility: Toast notifications
    function showToast(message, type = 'success') {
      const container = document.getElementById('toast-container');
      const toast = document.createElement('div');
      toast.className = `toast toast-${type} show`;
      toast.innerHTML = `
        <span>${type === 'success' ? '✨' : '⚠️'}</span>
        <span>${message}</span>
      `;
      container.appendChild(toast);
      setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
      }, 3000);
    }

    function switchFilterTab(type, btn) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById('filter-course-group').style.display = type === 'course' ? 'grid' : 'none';
      document.getElementById('filter-semester-group').style.display = type === 'semester' ? 'grid' : 'none';
      document.getElementById('filter-student-group').style.display = type === 'student' ? 'grid' : 'none';
      activeFilterType = type;
      discardSpreadsheetChanges();
      resetGridDisplay();
      let typeLabel = type;
      document.getElementById('analytics-subtitle').textContent = `Select a ${typeLabel} to load analytical distributions.`;
      document.querySelector('#analytics-empty p').textContent = `Real-time statistical averages, passed ratios, and grade bell curves populate once a ${typeLabel} is active.`;
      hideCourseAnalytics();
    }

    function resetGridDisplay() {
      document.getElementById('spreadsheet-wrap').style.display = 'none';
      document.getElementById('spreadsheet-empty').style.display = 'block';
      document.getElementById('spreadsheet-loading').style.display = 'none';
    }

    async function init() {
      const userJson = localStorage.getItem('user');
      if (userJson) {
        const user = JSON.parse(userJson);
        const navUsername = document.getElementById('nav-username');
        if (navUsername) navUsername.textContent = user.username;
        document.getElementById('hero-welcome').textContent = `Faculty Control: ${user.username}`;
      }
      await loadFilters();
    }

    async function loadFilters() {
      try {
        const courses = await API.getCourses();
        const courseSelect = document.getElementById('course-select');
        courseSelect.innerHTML = `<option value="">Choose an active course...</option>` +
          courses.map(c => `<option value="${c.id}">${c.courseCode} - ${c.courseName}</option>`).join('');
        const semesters = await API.getSemesters();
        const semesterSelect = document.getElementById('semester-select');
        semesterSelect.innerHTML = `<option value="">Choose a semester...</option>` +
          semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
        const students = await API.getStudents();
        const studentSelect = document.getElementById('student-select');
        studentSelect.innerHTML = `<option value="">Choose a student file...</option>` +
          students.map(s => `<option value="${s.id}">${s.name} (${s.studentId})</option>`).join('');
      } catch (err) {
        console.error(err);
        showToast('Failed to pull portal filters from backend.', 'error');
      }
    }

    async function loadSpreadsheet(type) {
      const loading = document.getElementById('spreadsheet-loading');
      const empty = document.getElementById('spreadsheet-empty');
      const wrap = document.getElementById('spreadsheet-wrap');
      loading.style.display = 'block';
      empty.style.display = 'none';
      wrap.style.display = 'none';
      discardSpreadsheetChanges();
      try {
        let enrollments = [];
        let headersHtml = '';
        if (type === 'course') {
          const courseId = document.getElementById('course-select').value;
          if (!courseId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsByCourse(courseId);
          headersHtml = `
            <th><span class="modified-indicator"></span>Student Name</th>
            <th>Student Roll</th>
            <th>Credits</th>
            <th style="width: 25%;">Assigned Grade Letter</th>
          `;
          loadCourseAnalytics(courseId);
        } else if (type === 'semester') {
          const semesterId = document.getElementById('semester-select').value;
          if (!semesterId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsBySemester(semesterId);
          headersHtml = `
            <th><span class="modified-indicator"></span>Student Name</th>
            <th>Course Code</th>
            <th>Course Name</th>
            <th style="width: 25%;">Assigned Grade Letter</th>
          `;
          const semesterSelect = document.getElementById('semester-select');
          const semesterText = semesterSelect.options[semesterSelect.selectedIndex].text;
          renderLocalAnalytics(semesterText, 'Semester Level Summary', enrollments);
        } else if (type === 'student') {
          const studentId = document.getElementById('student-select').value;
          if (!studentId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsByStudent(studentId);
          headersHtml = `
            <th><span class="modified-indicator"></span>Course Code</th>
            <th>Course Name</th>
            <th>Semester</th>
            <th style="width: 25%;">Assigned Grade Letter</th>
          `;
          const studentSelect = document.getElementById('student-select');
          const studentText = studentSelect.options[studentSelect.selectedIndex].text;
          renderLocalAnalytics(studentText, 'Student Level Summary', enrollments);
        }
        originalEnrollments = enrollments;
        const tbody = document.getElementById('spreadsheet-tbody');
        document.getElementById('spreadsheet-headers').innerHTML = headersHtml;
        if (enrollments.length === 0) {
          tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2rem; color: var(--slate-light);">No enrollment files found matching filter.</td></tr>`;
        } else {
          tbody.innerHTML = enrollments.map(e => {
            let col1 = '', col2 = '', col3 = '';
            if (type === 'course') {
              col1 = e.studentName || e.student?.name || 'N/A';
              col2 = e.studentRollNumber || e.student?.studentId || 'N/A';
              col3 = `${e.credits || e.course?.credits || 4} Credits`;
            } else if (type === 'semester') {
              col1 = e.studentName || e.student?.name || 'N/A';
              col2 = e.courseCode || e.course?.courseCode || 'N/A';
              col3 = e.courseName || e.course?.courseName || 'N/A';
            } else if (type === 'student') {
              col1 = e.courseCode || e.course?.courseCode || 'N/A';
              col2 = e.courseName || e.course?.courseName || 'N/A';
              col3 = `Semester ${e.semesterNumber || e.course?.semester?.semesterNumber || 'N/A'}`;
            }
            return `
              <tr id="row-${e.id}">
                <td>
                  <div style="display: flex; align-items: center;">
                    <span class="modified-indicator"></span>
                    <strong>${col1}</strong>
                  </div>
                </td>
                <td>${col2}</td>
                <td>${col3}</td>
                <td>
                  <select class="spreadsheet-select-custom" onchange="trackGradeChange(${e.id}, this)">
                    <option value="" ${!e.grade ? 'selected' : ''}>Not Graded</option>
                    <option value="O" ${e.grade==='O' ? 'selected' : ''}>O (Outstanding)</option>
                    <option value="A+" ${e.grade==='A+' ? 'selected' : ''}>A+ (Excellent)</option>
                    <option value="A" ${e.grade==='A' ? 'selected' : ''}>A (Very Good)</option>
                    <option value="B+" ${e.grade==='B+' ? 'selected' : ''}>B+ (Good)</option>
                    <option value="B" ${e.grade==='B' ? 'selected' : ''}>B (Above Average)</option>
                    <option value="C" ${e.grade==='C' ? 'selected' : ''}>C (Average)</option>
                    <option value="P" ${e.grade==='P' ? 'selected' : ''}>P (Pass)</option>
                    <option value="F" ${e.grade==='F' ? 'selected' : ''}>F (Fail)</option>
                  </select>
                </td>
              </tr>
            `;
          }).join('');
        }
        loading.style.display = 'none';
        wrap.style.display = 'block';
      } catch (err) {
        console.error(err);
        showToast('Failed to pull classroom spreadsheets.', 'error');
        resetGridDisplay();
      }
    }

    // Analytics functions (same as original)
    async function loadCourseAnalytics(courseId) {
      try {
        const analytics = await API.getCourseAnalytics(courseId);
        const courseSelect = document.getElementById('course-select');
        const courseText = courseSelect.options[courseSelect.selectedIndex].text;
        document.getElementById('anal-course-title').textContent = courseText.split(' - ')[1] || courseText;
        document.getElementById('anal-course-code').textContent = courseText.split(' - ')[0] || 'Course Details';
        document.getElementById('anal-average').textContent = (analytics.averageGrade || 0).toFixed(2);
        const total = analytics.totalStudents || 0;
        const passed = analytics.passed || 0;
        const passRate = total > 0 ? Math.round((passed / total) * 100) : 0;
        document.getElementById('anal-pass-rate').textContent = `${passRate}%`;
        const dist = analytics.gradeDistribution || {};
        const list = document.getElementById('grade-distribution-bars');
        list.innerHTML = '';
        const GRADES_LIST = ['O', 'A+', 'A', 'B+', 'B', 'C', 'P', 'F'];
        let maxCount = 1;
        GRADES_LIST.forEach(g => { if ((dist[g] || 0) > maxCount) maxCount = dist[g]; });
        GRADES_LIST.forEach(g => {
          const count = dist[g] || 0;
          const pct = total > 0 ? (count / maxCount) * 100 : 0;
          const barHtml = `
            <div class="chart-bar-item">
              <span class="chart-bar-label">${g} Grade</span>
              <div class="chart-bar-track">
                <div class="chart-bar-fill" style="width: 0%;" id="bar-fill-${g}"></div>
              </div>
              <span class="chart-bar-value">${count}</span>
            </div>
          `;
          list.insertAdjacentHTML('beforeend', barHtml);
          setTimeout(() => {
            const fill = document.getElementById(`bar-fill-${g}`);
            if (fill) fill.style.width = `${pct}%`;
          }, 50);
        });
        document.getElementById('analytics-empty').style.display = 'none';
        document.getElementById('analytics-content').style.display = 'block';
      } catch (err) {
        console.error(err);
        hideCourseAnalytics();
      }
    }

    function renderLocalAnalytics(title, subtitle, enrollments) {
      const GRADE_POINTS = { 'O':10, 'A+':9, 'A':8, 'B+':7, 'B':6, 'C':5, 'P':4, 'F':0 };
      document.getElementById('anal-course-title').textContent = title;
      document.getElementById('anal-course-code').textContent = subtitle;
      const graded = enrollments.filter(e => e.grade && GRADE_POINTS[e.grade] !== undefined);
      const total = graded.length;
      let sum = 0, passed = 0;
      const dist = { 'O':0, 'A+':0, 'A':0, 'B+':0, 'B':0, 'C':0, 'P':0, 'F':0 };
      graded.forEach(e => {
        const gp = GRADE_POINTS[e.grade];
        sum += gp;
        dist[e.grade]++;
        if (e.grade !== 'F') passed++;
      });
      const avg = total > 0 ? (sum / total) : 0;
      const passRate = total > 0 ? Math.round((passed / total) * 100) : 0;
      document.getElementById('anal-average').textContent = avg.toFixed(2);
      document.getElementById('anal-pass-rate').textContent = `${passRate}%`;
      const list = document.getElementById('grade-distribution-bars');
      list.innerHTML = '';
      const GRADES_LIST = ['O', 'A+', 'A', 'B+', 'B', 'C', 'P', 'F'];
      let maxCount = 1;
      GRADES_LIST.forEach(g => { if ((dist[g] || 0) > maxCount) maxCount = dist[g]; });
      GRADES_LIST.forEach(g => {
        const count = dist[g] || 0;
        const pct = total > 0 ? (count / maxCount) * 100 : 0;
        const barHtml = `
          <div class="chart-bar-item">
            <span class="chart-bar-label">${g} Grade</span>
            <div class="chart-bar-track">
              <div class="chart-bar-fill" style="width: 0%;" id="bar-fill-${g}"></div>
            </div>
            <span class="chart-bar-value">${count}</span>
          </div>
        `;
        list.insertAdjacentHTML('beforeend', barHtml);
        setTimeout(() => {
          const fill = document.getElementById(`bar-fill-${g}`);
          if (fill) fill.style.width = `${pct}%`;
        }, 50);
      });
      document.getElementById('analytics-empty').style.display = 'none';
      document.getElementById('analytics-content').style.display = 'block';
    }

    function hideCourseAnalytics() {
      document.getElementById('analytics-empty').style.display = 'block';
      document.getElementById('analytics-content').style.display = 'none';
    }

    function trackGradeChange(enrollmentId, select) {
      const row = document.getElementById(`row-${enrollmentId}`);
      const originalGrade = originalEnrollments.find(e => e.id === enrollmentId)?.grade || '';
      const newGrade = select.value;
      if (originalGrade === newGrade) {
        delete pendingChanges[enrollmentId];
        row.classList.remove('spreadsheet-row-modified');
      } else {
        pendingChanges[enrollmentId] = newGrade;
        row.classList.add('spreadsheet-row-modified');
      }
      renderBulkFloatingBar();
    }

    function renderBulkFloatingBar() {
      const bar = document.getElementById('bulk-bar');
      const count = Object.keys(pendingChanges).length;
      if (count > 0) {
        document.getElementById('modified-count-text').textContent = `${count} spreadsheet row${count > 1 ? 's' : ''} modified and pending sync.`;
        bar.classList.add('active');
      } else {
        bar.classList.remove('active');
      }
    }

    function discardSpreadsheetChanges() {
      pendingChanges = {};
      document.querySelectorAll('#spreadsheet-tbody tr').forEach(row => row.classList.remove('spreadsheet-row-modified'));
      originalEnrollments.forEach(e => {
        const row = document.getElementById(`row-${e.id}`);
        if (row) {
          const select = row.querySelector('.spreadsheet-select-custom');
          if (select) select.value = e.grade || '';
        }
      });
      renderBulkFloatingBar();
    }

    async function saveBulkChanges() {
      const count = Object.keys(pendingChanges).length;
      if (count === 0) return;
      const requests = Object.keys(pendingChanges).map(id => ({ enrollmentId: parseInt(id), grade: pendingChanges[id] || null }));
      const bar = document.getElementById('bulk-bar');
      bar.classList.remove('active');
      try {
        await API.updateGradesBulk(requests);
        showToast(`Successfully sync'd ${count} grade entries to university ledger.`, 'success');
        pendingChanges = {};
        setTimeout(() => { loadSpreadsheet(activeFilterType); }, 500);
      } catch (err) {
        console.error(err);
        showToast('Failed to save bulk changes. Check roles or connectivity.', 'error');
        bar.classList.add('active');
      }
    }

    function logout() {
      localStorage.clear();
      window.location.href = '/';
    }

    // Expose necessary functions to global scope for inline handlers
    window.switchFilterTab = switchFilterTab;
    window.loadSpreadsheet = loadSpreadsheet;
    window.trackGradeChange = trackGradeChange;
    window.discardSpreadsheetChanges = discardSpreadsheetChanges;
    window.saveBulkChanges = saveBulkChanges;
    window.logout = logout;

    // Kick off init after module load
    init();
  });
}
