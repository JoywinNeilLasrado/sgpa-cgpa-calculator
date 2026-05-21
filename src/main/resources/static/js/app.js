const tabButtons = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');

let students = [];
let semesters = [];
let courses = [];
let enrollments = [];
let currentDashboard = null;
let currentResultRows = [];

tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        tabButtons.forEach(b => b.classList.remove('active'));
        tabContents.forEach(c => c.style.display = 'none');

        btn.classList.add('active');
        document.getElementById(`${btn.dataset.tab}-section`).style.display = 'block';
    });
});

async function api(url, options = {}) {
    const res = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Request failed');
    }
    if (res.status === 204) {
        return null;
    }
    return res.json();
}

function showManageMessage(text, type = 'success') {
    const box = document.getElementById('manage-message');
    box.textContent = text;
    box.className = `message ${type}`;
}

function getFilterValue(id) {
    return (document.getElementById(id)?.value || '').trim().toLowerCase();
}

function matchesFilter(values, filter) {
    if (!filter) {
        return true;
    }
    return values.some(value => String(value || '').toLowerCase().includes(filter));
}

function fillSelect(id, items, label, placeholder) {
    const select = document.getElementById(id);
    if (!select) return;
    select.innerHTML = `<option value="">${placeholder}</option>`;
    items.forEach(item => {
        const opt = document.createElement('option');
        opt.value = item.id;
        opt.textContent = label(item);
        select.appendChild(opt);
    });
}

function renderTables() {
    const studentsFilter = getFilterValue('students-filter');
    const semestersFilter = getFilterValue('semesters-filter');
    const coursesFilter = getFilterValue('courses-filter');
    const enrollmentsFilter = getFilterValue('enrollments-filter');

    document.getElementById('students-table').innerHTML = students
        .filter(s => matchesFilter([s.name, s.studentId], studentsFilter))
        .map(s => `
            <tr>
                <td>${s.name}</td>
                <td>${s.studentId}</td>
                <td class="toolbar">
                    <button class="btn-secondary" onclick="editStudent(${s.id})">Edit</button>
                    <button class="btn-danger" onclick="deleteItem('/api/students/${s.id}', 'Student deleted')">Delete</button>
                </td>
            </tr>
        `).join('');

    document.getElementById('semesters-table').innerHTML = semesters
        .filter(s => matchesFilter([`Semester ${s.semesterNumber}`, s.semesterNumber], semestersFilter))
        .map(s => `
            <tr>
                <td>Semester ${s.semesterNumber}</td>
                <td class="toolbar">
                    <button class="btn-secondary" onclick="editSemester(${s.id})">Edit</button>
                    <button class="btn-danger" onclick="deleteItem('/api/semesters/${s.id}', 'Semester deleted')">Delete</button>
                </td>
            </tr>
        `).join('');

    document.getElementById('courses-table').innerHTML = courses
        .filter(c => matchesFilter([c.courseCode, c.courseName, c.credits, `Semester ${c.semester?.semesterNumber || ''}`], coursesFilter))
        .map(c => `
            <tr>
                <td>${c.courseCode}</td>
                <td>${c.courseName}</td>
                <td>${c.credits}</td>
                <td>Semester ${c.semester?.semesterNumber || ''}</td>
                <td class="toolbar">
                    <button class="btn-secondary" onclick="editCourse(${c.id})">Edit</button>
                    <button class="btn-danger" onclick="deleteItem('/api/courses/${c.id}', 'Course deleted')">Delete</button>
                </td>
            </tr>
        `).join('');

    document.getElementById('enrollments-table').innerHTML = enrollments
        .filter(e => matchesFilter([e.studentName, e.studentRollNumber, e.courseCode, e.courseName, e.grade, `Semester ${e.semesterNumber}`], enrollmentsFilter))
        .map(e => `
            <tr>
                <td>${e.studentName} (${e.studentRollNumber})</td>
                <td>${e.courseCode} - ${e.courseName}</td>
                <td>Semester ${e.semesterNumber}</td>
                <td><span class="grade-badge grade-${e.grade.replace('+', '-plus')}">${e.grade}</span></td>
                <td>${e.creditPoints}</td>
                <td class="toolbar">
                    <button class="btn-secondary" onclick="editEnrollment(${e.id})">Edit</button>
                    <button class="btn-danger" onclick="deleteItem('/api/enrollments/${e.id}', 'Enrollment deleted')">Delete</button>
                </td>
            </tr>
        `).join('');
}

async function loadData() {
    try {
        [students, semesters, courses, enrollments] = await Promise.all([
            api('/api/students'),
            api('/api/semesters'),
            api('/api/courses'),
            api('/api/enrollments')
        ]);

        semesters.sort((a, b) => a.semesterNumber - b.semesterNumber);
        courses.sort((a, b) => a.courseCode.localeCompare(b.courseCode));

        ['sgpa-student', 'cgpa-student', 'dashboard-student', 'enrollment-student'].forEach(id => {
            fillSelect(id, students, s => `${s.name} (${s.studentId})`, 'Choose a student...');
        });
        ['sgpa-semester', 'cgpa-semester', 'course-semester'].forEach(id => {
            fillSelect(id, semesters, s => `Semester ${s.semesterNumber}`, id === 'cgpa-semester' ? 'All Semesters' : 'Choose semester...');
        });
        fillSelect('enrollment-course', courses, c => `${c.courseCode} - ${c.courseName} (Sem ${c.semester?.semesterNumber || ''})`, 'Choose a course...');

        renderTables();
        await loadGradeScale();
    } catch (error) {
        console.error('Error loading data:', error);
        showManageMessage(error.message, 'error');
    }
}

async function loadGradeScale() {
    const tbody = document.getElementById('grade-table-body');
    if (tbody.children.length) return;

    const gradeScale = await api('/api/grade-scale');
    const gradeOrder = ['O', 'A+', 'A', 'B+', 'B', 'C', 'P', 'F'];
    gradeOrder.forEach(grade => {
        const g = gradeScale.grades[grade];
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><span class="grade-badge grade-${grade.replace('+', '-plus')}">${grade}</span></td>
            <td>${g.performance}</td>
            <td>${g.marks}</td>
            <td><strong>${g.points}</strong></td>
        `;
        tbody.appendChild(tr);
    });
}

async function calculateSGPA() {
    const studentId = document.getElementById('sgpa-student').value;
    const semesterId = document.getElementById('sgpa-semester').value;

    if (!studentId || !semesterId) {
        alert('Please select both student and semester');
        return;
    }

    document.getElementById('sgpa-result').style.display = 'block';
    document.getElementById('sgpa-value-display').textContent = '...';

    try {
        const data = await api(`/api/sgpa/student/${studentId}/semester/${semesterId}`);

        document.getElementById('sgpa-value-display').textContent = data.sgpa.toFixed(2);
        document.getElementById('sgpa-stats').innerHTML = `
            <div class="stat-item">
                <div class="stat-value">${data.totalCredits}</div>
                <div class="stat-label">Total Credits</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${data.totalCreditPoints}</div>
                <div class="stat-label">Credit Points</div>
            </div>
        `;

        document.getElementById('sgpa-result').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        alert(error.message);
    }
}

async function calculateCGPA() {
    const studentId = document.getElementById('cgpa-student').value;
    const semesterId = document.getElementById('cgpa-semester').value;

    if (!studentId) {
        alert('Please select a student');
        return;
    }

    document.getElementById('cgpa-result').style.display = 'block';
    document.getElementById('cgpa-value-display').textContent = '...';

    try {
        const url = semesterId
            ? `/api/cgpa/student/${studentId}/semester/${semesterId}`
            : `/api/cgpa/student/${studentId}`;

        const data = await api(url);

        document.getElementById('cgpa-value-display').textContent = data.cgpa.toFixed(2);
        document.getElementById('cgpa-stats').innerHTML = `
            <div class="stat-item">
                <div class="stat-value">${data.totalEarnedCredits}</div>
                <div class="stat-label">Credits Earned</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${data.totalCreditPoints}</div>
                <div class="stat-label">Credit Points</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${data.semestersCompleted}</div>
                <div class="stat-label">Semesters</div>
            </div>
        `;

        document.getElementById('cgpa-result').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        alert(error.message);
    }
}

async function loadDashboard() {
    const studentId = document.getElementById('dashboard-student').value;
    if (!studentId) {
        alert('Please select a student');
        return;
    }

    try {
        const dashboard = await api(`/api/students/${studentId}/dashboard`);
        currentDashboard = dashboard;
        document.getElementById('dashboard-result').style.display = 'block';
        document.getElementById('dashboard-name').textContent = dashboard.student.name;
        document.getElementById('dashboard-roll').textContent = dashboard.student.studentId;
        document.getElementById('dashboard-stats').innerHTML = `
            <div class="stat-item"><div class="stat-value">${dashboard.overallCgpa.toFixed(2)}</div><div class="stat-label">Overall CGPA</div></div>
            <div class="stat-item"><div class="stat-value">${dashboard.totalCredits}</div><div class="stat-label">Total Credits</div></div>
            <div class="stat-item"><div class="stat-value">${dashboard.semesters.length}</div><div class="stat-label">Semesters</div></div>
        `;
        document.getElementById('dashboard-semesters').innerHTML = dashboard.semesters.map(s => `
            <tr>
                <td>Semester ${s.semesterNumber}</td>
                <td>${s.sgpa.toFixed(2)}</td>
                <td>${s.totalCredits}</td>
                <td><button class="btn-secondary" onclick="loadSemesterResult(${studentId}, ${s.semesterId})">View Result</button></td>
            </tr>
        `).join('');
        if (dashboard.semesters.length) {
            loadSemesterResult(studentId, dashboard.semesters[0].semesterId);
        }
    } catch (error) {
        alert(error.message);
    }
}

async function loadSemesterResult(studentId, semesterId) {
    const rows = await api(`/api/results/student/${studentId}/semester/${semesterId}`);
    currentResultRows = rows;
    document.getElementById('dashboard-result-table').innerHTML = rows.map(r => `
        <tr>
            <td>${r.courseCode}</td>
            <td>${r.courseName}</td>
            <td>${r.credits}</td>
            <td><span class="grade-badge grade-${r.grade.replace('+', '-plus')}">${r.grade}</span></td>
            <td>${r.gradePoints}</td>
            <td>${r.creditPoints}</td>
        </tr>
    `).join('');
}

function exportDashboardReport() {
    if (!currentDashboard) {
        alert('Load a dashboard first');
        return;
    }

    const rows = currentResultRows.map(r => `
        <tr>
            <td>${r.courseCode}</td>
            <td>${r.courseName}</td>
            <td>${r.credits}</td>
            <td>${r.grade}</td>
            <td>${r.gradePoints}</td>
            <td>${r.creditPoints}</td>
        </tr>
    `).join('');

    const report = window.open('', '_blank');
    report.document.write(`
        <html>
            <head>
                <title>${currentDashboard.student.name} Transcript</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #111; }
                    h1, h2 { margin-bottom: 4px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background: #f3f4f6; }
                    .summary { display: flex; gap: 24px; margin: 20px 0; }
                    .summary div { border: 1px solid #ddd; padding: 12px 16px; }
                </style>
            </head>
            <body>
                <h1>${currentDashboard.student.name}</h1>
                <p>Roll Number: ${currentDashboard.student.studentId}</p>
                <div class="summary">
                    <div><strong>Overall CGPA</strong><br>${currentDashboard.overallCgpa.toFixed(2)}</div>
                    <div><strong>Total Credits</strong><br>${currentDashboard.totalCredits}</div>
                    <div><strong>Semesters</strong><br>${currentDashboard.semesters.length}</div>
                </div>
                <h2>Current Semester Result</h2>
                <table>
                    <thead>
                        <tr><th>Course Code</th><th>Course Name</th><th>Credits</th><th>Grade</th><th>Grade Points</th><th>Credit Points</th></tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
                <script>window.print();</script>
            </body>
        </html>
    `);
    report.document.close();
}

async function convertMarksToGrade() {
    const marksInput = document.getElementById('enrollment-marks');
    const result = document.getElementById('marks-grade-result');
    const marks = marksInput.value;
    if (marks === '') {
        result.textContent = '';
        return;
    }

    try {
        const data = await api(`/api/grades/from-marks?marks=${encodeURIComponent(marks)}`);
        document.getElementById('enrollment-grade').value = data.grade;
        result.textContent = `${data.grade} - ${data.performance} (${data.points} points)`;
    } catch (error) {
        result.textContent = error.message;
    }
}

async function createStudent() {
    try {
        await api('/api/students', {
            method: 'POST',
            body: JSON.stringify({
                name: document.getElementById('student-name').value,
                studentId: document.getElementById('student-roll').value
            })
        });
        document.getElementById('student-name').value = '';
        document.getElementById('student-roll').value = '';
        showManageMessage('Student added');
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

async function createSemester() {
    try {
        await api('/api/semesters', {
            method: 'POST',
            body: JSON.stringify({ semesterNumber: Number(document.getElementById('semester-number').value) })
        });
        document.getElementById('semester-number').value = '';
        showManageMessage('Semester added');
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

async function createCourse() {
    try {
        await api('/api/courses', {
            method: 'POST',
            body: JSON.stringify({
                courseCode: document.getElementById('course-code').value,
                courseName: document.getElementById('course-name').value,
                credits: Number(document.getElementById('course-credits').value),
                semesterId: document.getElementById('course-semester').value
            })
        });
        ['course-code', 'course-name', 'course-credits'].forEach(id => document.getElementById(id).value = '');
        showManageMessage('Course added');
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

async function createEnrollment() {
    try {
        await api('/api/enrollments', {
            method: 'POST',
            body: JSON.stringify({
                studentId: Number(document.getElementById('enrollment-student').value),
                courseId: Number(document.getElementById('enrollment-course').value),
                grade: document.getElementById('enrollment-grade').value
            })
        });
        document.getElementById('enrollment-marks').value = '';
        document.getElementById('marks-grade-result').textContent = '';
        showManageMessage('Enrollment added');
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

async function deleteItem(url, successMessage) {
    if (!confirm('Delete this item? Related records may also be removed.')) return;
    try {
        await api(url, { method: 'DELETE' });
        showManageMessage(successMessage);
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

async function editStudent(id) {
    const student = students.find(s => s.id === id);
    const name = prompt('Student name', student.name);
    if (name === null) return;
    const roll = prompt('Roll number', student.studentId);
    if (roll === null) return;
    await updateItem(`/api/students/${id}`, { name, studentId: roll }, 'Student updated');
}

async function editSemester(id) {
    const semester = semesters.find(s => s.id === id);
    const semesterNumber = prompt('Semester number', semester.semesterNumber);
    if (semesterNumber === null) return;
    await updateItem(`/api/semesters/${id}`, { semesterNumber: Number(semesterNumber) }, 'Semester updated');
}

async function editCourse(id) {
    const course = courses.find(c => c.id === id);
    const courseCode = prompt('Course code', course.courseCode);
    if (courseCode === null) return;
    const courseName = prompt('Course name', course.courseName);
    if (courseName === null) return;
    const credits = prompt('Credits', course.credits);
    if (credits === null) return;
    await updateItem(`/api/courses/${id}`, {
        courseCode,
        courseName,
        credits: Number(credits),
        semesterId: course.semester.id
    }, 'Course updated');
}

async function editEnrollment(id) {
    const enrollment = enrollments.find(e => e.id === id);
    const grade = prompt('Grade (O, A+, A, B+, B, C, P, F)', enrollment.grade);
    if (grade === null) return;
    await updateItem(`/api/enrollments/${id}`, {
        studentId: enrollment.studentId,
        courseId: enrollment.courseId,
        grade
    }, 'Enrollment updated');
}

async function updateItem(url, payload, successMessage) {
    try {
        await api(url, { method: 'PUT', body: JSON.stringify(payload) });
        showManageMessage(successMessage);
        await loadData();
    } catch (error) {
        showManageMessage(error.message, 'error');
    }
}

loadData();
