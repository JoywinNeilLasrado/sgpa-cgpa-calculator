// Faculty Events Module
// This module encapsulates all faculty page logic previously in faculty.js
// Export a single init function to be called by the thin bootstrap script.

import { Toast } from '../components/toast.js';
import { Auth } from '../auth/Auth.js';
import { Store } from '../state/store.js';
import { handleAPIError } from '../services/errorHandler.js';

export function initFaculty() {
  let pendingChanges = {}; // Maps enrollmentId -> new marks object
  let originalEnrollments = [];
  let cachedCourses = [];
  let activeFilterType = 'course'; // course, semester, student

  // Check auth state
  if (!Auth.requireRole('FACULTY')) {
    return;
  }

  // Import API service dynamically (avoid top-level import to keep this module pure)
  // Assuming apiService.js is an ES module exporting { apiService as API }
  // We'll import it here.
  import('../services/apiService.js').then(({ apiService: API }) => {
    // Helper to calculate CIE dynamically
    function calculateCie(cType, t1, t2, assign, oaa, reg, lt, lr) {
      t1 = t1 ?? 0;
      t2 = t2 ?? 0;
      assign = assign ?? 0;
      oaa = oaa ?? 0;
      reg = reg ?? 0;
      lt = lt ?? 0;
      lr = lr ?? 0;

      if (cType === 'THEORY') {
        const avgTests = Math.round((t1 + t2) * 0.3);
        return avgTests + assign + oaa;
      } else if (cType === 'LABORATORY') {
        return reg + lt + lr;
      } else if (cType === 'INTEGRATED') {
        const avgTests = Math.round((t1 + t2) * 0.3);
        const theoryTotal = avgTests + assign + oaa;
        const theoryReduced = Math.round(theoryTotal * 0.6);
        const labTotal = reg + lt + lr;
        const labReduced = Math.round(labTotal * 0.4);
        return theoryReduced + labReduced;
      }
      return 0;
    }

    // Local showToast delegation to centralized Toast module
    function showToast(message, type = 'success') {
      Toast.show(message, type);
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
      setupEventListeners();
      await loadFilters();
      if (window.hidePageLoader) window.hidePageLoader();
    }

    function setupEventListeners() {
      // Tab switching
      document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          const type = btn.dataset.tab;
          if (type) {
            switchFilterTab(type, btn);
          }
        });
      });

      // Dropdown changes
      const courseSelect = document.getElementById('course-select');
      if (courseSelect) {
        courseSelect.addEventListener('change', () => loadSpreadsheet('course'));
      }

      const semesterSelect = document.getElementById('semester-select');
      if (semesterSelect) {
        semesterSelect.addEventListener('change', () => loadSpreadsheet('semester'));
      }

      const studentSelect = document.getElementById('student-select');
      if (studentSelect) {
        studentSelect.addEventListener('change', () => loadSpreadsheet('student'));
      }

      // Spreadsheet inputs delegation (bubbling)
      const tbody = document.getElementById('spreadsheet-tbody');
      if (tbody) {
        tbody.addEventListener('input', (e) => {
          const markInput = e.target.closest('.mark-input');
          if (markInput) {
            const enrollmentId = parseInt(markInput.dataset.enrollmentId);
            trackGradeChange(enrollmentId);
          }
        });
      }

      // Bulk actions
      const discardBtn = document.querySelector('#bulk-bar .discard-btn');
      if (discardBtn) {
        discardBtn.addEventListener('click', discardSpreadsheetChanges);
      }

      const saveBtn = document.querySelector('#bulk-bar .save-btn');
      if (saveBtn) {
        saveBtn.addEventListener('click', saveBulkChanges);
      }
    }

    async function loadFilters() {
      try {
        cachedCourses = await API.getFacultyCourses();
        Store.set('courses', cachedCourses);
        const courseSelect = document.getElementById('course-select');
        courseSelect.innerHTML = `<option value="">Choose an active course...</option>` +
          cachedCourses.map(c => `<option value="${c.id}">${c.courseCode} - ${c.courseName}</option>`).join('');
        const semesters = await API.getSemesters();
        Store.set('semesters', semesters);
        const semesterSelect = document.getElementById('semester-select');
        semesterSelect.innerHTML = `<option value="">Choose a semester...</option>` +
          semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
        const students = await API.getStudents();
        Store.set('students', students);
        const studentSelect = document.getElementById('student-select');
        studentSelect.innerHTML = `<option value="">Choose a student file...</option>` +
          students.map(s => `<option value="${s.id}">${s.name} (${s.studentId})</option>`).join('');

        // Initialize TomSelect for searchability
        const tsConfig = {
            create: false,
            sortField: { field: "text", direction: "asc" },
            placeholder: "Type to search...",
            allowEmptyOption: true
        };
        if (window.TomSelect) {
            new TomSelect(courseSelect, tsConfig);
            new TomSelect(semesterSelect, tsConfig);
            new TomSelect(studentSelect, tsConfig);
        }
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
          loadCourseAnalytics(courseId);
        } else if (type === 'semester') {
          const semesterId = document.getElementById('semester-select').value;
          if (!semesterId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsBySemester(semesterId);
          const semesterSelect = document.getElementById('semester-select');
          renderLocalAnalytics(semesterSelect.options[semesterSelect.selectedIndex].text, 'Semester Level Summary', enrollments);
        } else if (type === 'student') {
          const studentId = document.getElementById('student-select').value;
          if (!studentId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsByStudent(studentId);
          const studentSelect = document.getElementById('student-select');
          renderLocalAnalytics(studentSelect.options[studentSelect.selectedIndex].text, 'Student Level Summary', enrollments);
        }
        
        originalEnrollments = enrollments;
        const tbody = document.getElementById('spreadsheet-tbody');
        const thead = document.getElementById('spreadsheet-headers');

        if (enrollments.length === 0) {
          thead.innerHTML = '<th>No Data</th>';
          tbody.innerHTML = '<tr><td colspan="15" style="text-align: center; padding: 2rem; color: var(--slate-light);">No enrollment files found matching filter.</td></tr>';
        } else {
          // Determine dominant course type if possible (especially for course filter)
          const firstCourseType = enrollments[0].courseType || 'THEORY';
          const isMixed = type !== 'course';
          
          let hHtml = '';
          if (type === 'course') {
              hHtml += '<th><span class="modified-indicator"></span>Student Name</th><th>Student Roll</th><th>Credits</th>';
          } else if (type === 'semester') {
              hHtml += '<th><span class="modified-indicator"></span>Student Name</th><th>Course Code</th><th>Course Name</th>';
          } else {
              hHtml += '<th><span class="modified-indicator"></span>Course Code</th><th>Course Name</th><th>Semester</th>';
          }

          if (isMixed || firstCourseType === 'THEORY' || firstCourseType === 'INTEGRATED') {
              hHtml += '<th style="width: 7%;">Test 1 (50)</th><th style="width: 7%;">Test 2 (50)</th><th style="width: 7%;">Assign (10)</th><th style="width: 7%;">OAA (10)</th>';
          }
          if (isMixed || firstCourseType === 'LABORATORY' || firstCourseType === 'INTEGRATED') {
              hHtml += '<th style="width: 7%;">Reg Lab (20)</th><th style="width: 7%;">Lab Test (20)</th><th style="width: 7%;">Lab Rec (10)</th>';
          }
          hHtml += '<th style="width: 6%; text-align: center;">CIE (50)</th><th style="width: 8%;">SEE (50)</th><th style="width: 6%;">Grace (5)</th><th style="width: 10%;">Grade</th>';
          
          thead.innerHTML = hHtml;

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
            
            const cType = e.courseType || 'THEORY';
            const hasTheory = cType === 'THEORY' || cType === 'INTEGRATED';
            const hasLab = cType === 'LABORATORY' || cType === 'INTEGRATED';
            
            let rowHtml = `<tr id="row-${e.id}">`;
            rowHtml += `<td><div style="display: flex; align-items: center;"><span class="modified-indicator"></span><strong>${col1}</strong></div></td><td>${col2}</td><td>${col3}</td>`;
            
            const renderInput = (field, val, max, enabled) => {
                if (!enabled) return `<td><input type="number" class="spreadsheet-select-custom mark-input disabled-input" disabled title="N/A" value=""></td>`;
                return `<td><input type="number" class="spreadsheet-select-custom mark-input" data-field="${field}" data-enrollment-id="${e.id}" value="${val ?? ''}" min="0" max="${max}"></td>`;
            };

            if (isMixed || firstCourseType === 'THEORY' || firstCourseType === 'INTEGRATED') {
                rowHtml += renderInput('test1Marks', e.test1Marks, 50, hasTheory);
                rowHtml += renderInput('test2Marks', e.test2Marks, 50, hasTheory);
                rowHtml += renderInput('assignmentMarks', e.assignmentMarks, 10, hasTheory);
                rowHtml += renderInput('oaaMarks', e.oaaMarks, 10, hasTheory);
            }
            if (isMixed || firstCourseType === 'LABORATORY' || firstCourseType === 'INTEGRATED') {
                rowHtml += renderInput('regularLabMarks', e.regularLabMarks, 20, hasLab);
                rowHtml += renderInput('labTestMarks', e.labTestMarks, 20, hasLab);
                rowHtml += renderInput('labRecordMarks', e.labRecordMarks, 10, hasLab);
            }

            // Calculate dynamic CIE
            const cie = calculateCie(cType, e.test1Marks, e.test2Marks, e.assignmentMarks, e.oaaMarks, e.regularLabMarks, e.labTestMarks, e.labRecordMarks);

            rowHtml += `<td style="font-weight: bold; text-align: center; vertical-align: middle;"><span class="cie-cell" id="cie-${e.id}" data-type="${cType}">${cie}</span></td>`;
            rowHtml += renderInput('seeMarks', e.seeMarks, 50, true);
            rowHtml += renderInput('graceMarks', e.graceMarks, 5, true);
            rowHtml += `<td><span class="grade-badge ${e.grade || 'none'}">${e.grade || 'Pending'}</span></td></tr>`;
            
            return rowHtml;
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

    function trackGradeChange(enrollmentId) {
      const row = document.getElementById(`row-${enrollmentId}`);
      if (!row) return;
      
      const inputs = row.querySelectorAll('.mark-input');
      let changed = false;
      const changes = {};
      
      const original = originalEnrollments.find(e => e.id === enrollmentId) || {};
      
      inputs.forEach(inp => {
        if (!inp.disabled) {
          const field = inp.dataset.field;
          const val = inp.value === '' ? null : parseInt(inp.value);
          const origVal = original[field] ?? null;
          
          if (val !== origVal) {
            changed = true;
          }
          changes[field] = val;
        }
      });

      // Recalculate dynamic CIE inside the browser
      const getVal = (field) => {
        const el = row.querySelector(`[data-field="${field}"]`);
        if (!el || el.value === '' || el.disabled) return 0;
        return parseInt(el.value) || 0;
      };

      const cieCell = document.getElementById(`cie-${enrollmentId}`);
      if (cieCell) {
        const cType = cieCell.getAttribute('data-type') || 'THEORY';
        const t1 = getVal('test1Marks');
        const t2 = getVal('test2Marks');
        const assign = getVal('assignmentMarks');
        const oaa = getVal('oaaMarks');
        const reg = getVal('regularLabMarks');
        const lt = getVal('labTestMarks');
        const lr = getVal('labRecordMarks');
        
        const newCie = calculateCie(cType, t1, t2, assign, oaa, reg, lt, lr);
        cieCell.textContent = newCie;
      }
      
      if (!changed) {
        delete pendingChanges[enrollmentId];
        row.classList.remove('spreadsheet-row-modified');
      } else {
        pendingChanges[enrollmentId] = changes;
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
          row.querySelectorAll('.mark-input').forEach(inp => {
            const field = inp.dataset.field;
            inp.value = e[field] ?? '';
          });
          
          // Recalculate original CIE cell
          const cieCell = document.getElementById(`cie-${e.id}`);
          if (cieCell) {
            const cType = cieCell.getAttribute('data-type') || 'THEORY';
            const cie = calculateCie(cType, e.test1Marks, e.test2Marks, e.assignmentMarks, e.oaaMarks, e.regularLabMarks, e.labTestMarks, e.labRecordMarks);
            cieCell.textContent = cie;
          }
        }
      });
      renderBulkFloatingBar();
    }

    async function saveBulkChanges() {
      const count = Object.keys(pendingChanges).length;
      if (count === 0) return;
      const requests = Object.keys(pendingChanges).map(id => ({ 
        enrollmentId: parseInt(id), 
        ...pendingChanges[id]
      }));
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
      Auth.logout();
    }

    // Kick off init after module load
    init();
  });
}
