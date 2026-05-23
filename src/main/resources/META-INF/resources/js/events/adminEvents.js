// events/adminEvents.js
import { apiService as API } from '../services/apiService.js';
import { renderStudentList } from '../ui/uiRenderer.js';
import { UI } from '../ui.js';

function openPasswordModal(username) {
    const modal = document.getElementById('passwordModal');
    modal.dataset.username = username;
    modal.classList.add('active');
}

function closePasswordModal() {
    const modal = document.getElementById('passwordModal');
    modal.classList.remove('active');
    document.getElementById('newPasswordInput').value = '';
}

function submitPasswordChange() {
    const username = document.getElementById('passwordModal').dataset.username;
    const newPass = document.getElementById('newPasswordInput').value.trim();
    if (!newPass) {
        showToast('Password cannot be empty', 'error');
        return;
    }
    API.changeUserPassword(username, newPass).then(() => {
        showToast('Password changed successfully');
        closePasswordModal();
    }).catch(err => {
        console.error(err);
        showToast('Failed to change password', 'error');
    });
}

let cachedStudents = [];
let cachedCourses = [];
let cachedEnrollments = [];
let cachedDepartments = [];
let cachedSemesters = [];
let cachedFaculty = [];

// Check if token exists
if (!localStorage.getItem('token')) {
    window.location.href = '/';
}

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

function switchTab(tabId, btn) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.sidebar-item').forEach(el => el.classList.remove('active'));

    // Show current
    document.getElementById(`tab-${tabId}`).classList.add('active');
    btn.classList.add('active');
}

async function init() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
        const user = JSON.parse(userJson);
        const navUsername = document.getElementById('nav-username');
        const navRole = document.getElementById('nav-role');
        if (navUsername) navUsername.textContent = user.username;
        if (navRole) navRole.textContent = user.role;
    }

    await loadAllData();
}

async function loadAllData() {
    await loadDepartments();
    await loadStudents();
    await loadSemesters();
    await loadCourses();
    await loadFaculty();
    await loadEnrollments();

    // Sync summary counters
    document.getElementById('count-students').textContent = cachedStudents.length;
    document.getElementById('count-courses').textContent = cachedCourses.length;
    document.getElementById('count-enrollments').textContent = cachedEnrollments.length;
}

async function loadStudents() {
    try {
        const students = await API.getStudents();
        cachedStudents = students;

        const tbody = document.querySelector('#students-table tbody');
        document.getElementById('count-students-text').textContent = `${students.length} profile files siphoned`;

        renderStudentList(tbody, students);

        // Populate Dropdowns
        const enrollSelect = document.getElementById('enrollment-student');
        const gradesSelect = document.getElementById('grades-student');
        const options = students.map(s => `<option value="${s.id}">${s.name} (${s.studentId})</option>`).join('');
        enrollSelect.innerHTML = '<option value="">Select student target...</option>' + options;
        gradesSelect.innerHTML = '<option value="">Select student file...</option>' + options;

    } catch (err) {
        console.error(err);
        showToast('Failed to pull student accounts.', 'error');
    }
}

async function loadSemesters() {
    try {
        const semesters = await API.getSemesters();
        cachedSemesters = semesters;
        document.getElementById('count-semesters').textContent = semesters.length;

        const tbody = document.querySelector('#semesters-table tbody');
        tbody.innerHTML = semesters.length ? semesters.map(s => `
            <tr>
                <td><strong>${s.id}</strong></td>
                <td>Semester Stage ${s.semesterNumber}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm" style="margin-right: 0.5rem;" onclick="editSemester(${s.id}, ${s.semesterNumber})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteSemester(${s.id})">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="3" class="empty-state">No semester cycles defined.</td></tr>';

        // Populate semester dropdown
        const select = document.getElementById('course-semester');
        select.innerHTML = semesters.map(s => `<option value="${s.id}">Semester Stage ${s.semesterNumber}</option>`).join('');

    } catch (err) {
        console.error(err);
        showToast('Failed to load semesters.', 'error');
    }
}

async function loadCourses() {
    try {
        const courses = await API.getCourses();
        cachedCourses = courses;

        const tbody = document.querySelector('#courses-table tbody');
        tbody.innerHTML = courses.length ? courses.map(c => `
            <tr class="course-row-item" data-code="${c.code.toLowerCase()}" data-name="${c.name.toLowerCase()}">
                <td><strong>${c.code}</strong></td>
                <td>${c.name}</td>
                <td>${c.credits} Credits</td>
                <td><span class="grade-badge O">Sem ${c.semester?.semesterNumber || '-'}</span></td>
                <td>${c.faculty?.name || '-'}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm" style="margin-right: 0.5rem;" onclick="editCourse(${c.id}, '${c.code}', '${c.name.replace(/'/g, "\\'")}', ${c.credits}, ${c.semester?.id || 0}, ${c.faculty?.id || 0})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteCourse(${c.id})">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="6" class="empty-state">Curriculum inventory is empty.</td></tr>';

        // Populate enroll course dropdown
        const select = document.getElementById('enrollment-course');
        select.innerHTML = '<option value="">Select syllabus course...</option>' +
            courses.map(c => `<option value="${c.id}">${c.code} - ${c.name}</option>`).join('');

    } catch (err) {
        console.error(err);
        showToast('Failed to pull curriculum list.', 'error');
    }
}

async function loadFaculty() {
    try {
        const faculty = await API.getFacultyMembers();
        cachedFaculty = faculty;
        // Populate faculty dropdown for course assignment
        const select = document.getElementById('course-faculty');
        select.innerHTML = '<option value="">Select faculty...</option>' + faculty.map(f => `<option value="${f.id}">${f.name}</option>`).join('');
        // Populate faculty table
        const tbody = document.querySelector('#faculty-table tbody');
        tbody.innerHTML = faculty.length ? faculty.map(f => `
            <tr class="faculty-row-item" data-name="${f.name.toLowerCase()}" data-username="${f.username?.toLowerCase() || ''}">
                <td><strong>${f.id}</strong></td>
                <td>${f.name}</td>
                <td>${f.username}</td>
                <td><code>••••</code></td>
                <td>${f.email || '-'}</td>
                <td>${f.department || '-'}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm" style="margin-right: 0.5rem;" onclick="editFaculty(${f.id}, '${f.name}', '${f.username}', '${f.email || ''}', '${f.department || ''}')">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteFaculty(${f.id})">Delete</button>
                    <button class="btn btn-warning btn-sm" onclick="openPasswordModal('${f.username}')">Change Password</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="7" class="empty-state">No faculty accounts registered.</td></tr>';
        document.getElementById('count-faculty-text').textContent = `${faculty.length} faculty members loaded`;
    } catch (err) {
        console.error(err);
        showToast('Failed to load faculty list.', 'error');
    }
}

async function loadDepartments() {
    try {
        const departments = await API.getDepartments();
        cachedDepartments = departments;

        document.getElementById('count-departments-text').textContent = `${departments.length} departments loaded`;

        const tbody = document.querySelector('#departments-table tbody');
        tbody.innerHTML = departments.length ? departments.map(d => `
            <tr class="department-row-item" data-name="${d.name.toLowerCase()}" data-code="${d.code.toLowerCase()}">
                <td><strong>${d.id}</strong></td>
                <td>${d.name}</td>
                <td><span class="grade-badge A">${d.code}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm" style="margin-right: 0.5rem;" onclick="editDepartment(${d.id}, '${d.name}', '${d.code}')">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteDepartment(${d.id})">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="4" class="empty-state">No departments registered.</td></tr>';

        // Populate dynamic select dropdowns
        const studentBranchSelect = document.getElementById('student-branch');
        const facultyDeptSelect = document.getElementById('faculty-department');
        const optionsHtml = departments.map(d => `<option value="${d.name}">${d.name}</option>`).join('');
        studentBranchSelect.innerHTML = optionsHtml;
        facultyDeptSelect.innerHTML = optionsHtml;

    } catch (err) {
        console.error(err);
        showToast('Failed to pull departments.', 'error');
    }
}

async function loadEnrollments() {
    try {
        const enrollments = await API.getEnrollments();
        cachedEnrollments = enrollments;

        const tbody = document.querySelector('#enrollments-table tbody');
        tbody.innerHTML = enrollments.length ? enrollments.map(e => `
            <tr class="enroll-row-item" data-student="${(e.student?.name || '').toLowerCase()}" data-code="${(e.course?.code || '').toLowerCase()}">
                <td><strong>${e.student?.name || 'N/A'}</strong></td>
                <td>${e.course?.code || 'N/A'} - ${e.course?.name || 'N/A'}</td>
                <td><span class="grade-badge ${e.grade || 'none'}">${e.grade || 'Pending'}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-danger btn-sm" onclick="deleteEnrollment(${e.id})">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="4" class="empty-state">No course registrations active.</td></tr>';

    } catch (err) {
        console.error(err);
        showToast('Failed to load system registrations.', 'error');
    }
}

// Search Filter Utilities
function filterTable(type) {
    const query = document.getElementById(`search-${type}`).value.toLowerCase().trim();
    if (type === 'students') {
        document.querySelectorAll('.student-row-item').forEach(tr => {
            const name = tr.getAttribute('data-name');
            const roll = tr.getAttribute('data-roll');
            tr.style.display = (name.includes(query) || roll.includes(query)) ? '' : 'none';
        });
    } else if (type === 'courses') {
        document.querySelectorAll('.course-row-item').forEach(tr => {
            const name = tr.getAttribute('data-name');
            const code = tr.getAttribute('data-code');
            tr.style.display = (name.includes(query) || code.includes(query)) ? '' : 'none';
        });
    } else if (type === 'enrollments') {
        document.querySelectorAll('.enroll-row-item').forEach(tr => {
            const student = tr.getAttribute('data-student');
            const code = tr.getAttribute('data-code');
            tr.style.display = (student.includes(query) || code.includes(query)) ? '' : 'none';
        });
    } else if (type === 'faculty') {
        document.querySelectorAll('.faculty-row-item').forEach(tr => {
            const name = tr.getAttribute('data-name');
            const username = tr.getAttribute('data-username');
            tr.style.display = (name.includes(query) || username.includes(query)) ? '' : 'none';
        });
    } else if (type === 'departments') {
        document.querySelectorAll('.department-row-item').forEach(tr => {
            const name = tr.getAttribute('data-name');
            const code = tr.getAttribute('data-code');
            tr.style.display = (name.includes(query) || code.includes(query)) ? '' : 'none';
        });
    }
}

// Mutation Actions
async function addStudent() {
    const name = document.getElementById('student-name').value.trim();
    const studentId = document.getElementById('student-id').value.trim();
    const branch = document.getElementById('student-branch').value;
    const dob = document.getElementById('student-dob').value;
    if (!name || !studentId || !dob) { showToast('Complete all floating fields.', 'error'); return; }
    try {
        await API.createStudent(name, studentId, branch, dob);
        document.getElementById('student-name').value = '';
        document.getElementById('student-id').value = '';
        document.getElementById('student-dob').value = '2004-01-01';
        await loadStudents();
        showToast(`Student profile '${name}' registered successfully!`, 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to register student record.', 'error');
    }
}

async function addFaculty() {
    const name = document.getElementById('faculty-name').value.trim();
    const username = document.getElementById('faculty-username').value.trim();
    const email = document.getElementById('faculty-email').value.trim();
    const department = document.getElementById('faculty-department').value.trim();
    if (!name || !username || !email || !department) { showToast('Complete all faculty fields.', 'error'); return; }
    try {
        const password = prompt('Enter a password for the new faculty member:', 'password123');
        if (password === null) return;
        if (!password.trim()) { showToast('Password cannot be empty.', 'error'); return; }
        await API.createFaculty(name, username, password.trim(), email, department);
        document.getElementById('faculty-name').value = '';
        document.getElementById('faculty-username').value = '';
        document.getElementById('faculty-email').value = '';
        document.getElementById('faculty-department').value = '';
        await loadFaculty();
        showToast(`Faculty staff '${name}' onboarded successfully!`, 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to onboard faculty staff.', 'error');
    }
}

async function addDepartment() {
    const name = document.getElementById('department-name').value.trim();
    const code = document.getElementById('department-code').value.trim();
    if (!name || !code) { showToast('Complete all department fields.', 'error'); return; }
    try {
        await API.createDepartment(name, code);
        document.getElementById('department-name').value = '';
        document.getElementById('department-code').value = '';
        await loadDepartments();
        showToast(`Department '${name}' created successfully!`, 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to create department. Duplicates?', 'error');
    }
}

async function addSemester() {
    const num = document.getElementById('semester-number').value.trim();
    if (!num) { showToast('Provide a semester number cycle.', 'error'); return; }
    try {
        await API.createSemester(parseInt(num));
        document.getElementById('semester-number').value = '';
        await loadSemesters();
        showToast(`Academic Semester Stage ${num} established!`, 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to establish semester stage.', 'error');
    }
}

async function addCourse() {
    const code = document.getElementById('course-code').value.trim();
    const name = document.getElementById('course-name').value.trim();
    const credits = document.getElementById('course-credits').value.trim();
    const semesterId = document.getElementById('course-semester').value;
    const facultyId = document.getElementById('course-faculty').value;
    if (!code || !name || !credits || !semesterId || !facultyId) { showToast('Complete all course fields.', 'error'); return; }
    try {
        await API.createCourse(code, name, parseInt(credits), semesterId, facultyId);
        document.getElementById('course-code').value = '';
        document.getElementById('course-name').value = '';
        document.getElementById('course-credits').value = '';
        await loadCourses();
        showToast(`Syllabus item '${code}: ${name}' created successfully.`, 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to create syllabus course item.', 'error');
    }
}

async function addEnrollment() {
    const studentId = document.getElementById('enrollment-student').value;
    const courseId = document.getElementById('enrollment-course').value;
    if (!studentId || !courseId) { showToast('Choose student target and course mapping.', 'error'); return; }
    try {
        await API.createEnrollment(studentId, courseId);
        await loadEnrollments();
        showToast('Course registration siphoned successfully.', 'success');
    } catch (err) {
        console.error(err);
        showToast('Failed to map student registration. Duplicates?', 'error');
    }
}

// Destructive actions
async function deleteStudent(id) {
    if (confirm('Verify: Permanently purge student record and related grades files?')) {
        try {
            await API.deleteStudent(id);
            await loadStudents();
            showToast('Student file successfully purged.', 'success');
        } catch (err) {
            showToast('Purge rejected by data locks.', 'error');
        }
    }
}

async function deleteFaculty(id) {
    if (confirm('Verify: Permanently purge faculty member account?')) {
        try {
            await API.deleteFaculty(id);
            await loadFaculty();
            showToast('Faculty member successfully purged.', 'success');
        } catch (err) {
            console.error(err);
            showToast('Failed to delete faculty member.', 'error');
        }
    }
}

async function deleteDepartment(id) {
    if (confirm('Verify: Permanently delete this department?')) {
        try {
            await API.deleteDepartment(id);
            await loadDepartments();
            showToast('Department successfully deleted.', 'success');
        } catch (err) {
            showToast('Failed to delete department.', 'error');
        }
    }
}

async function deleteSemester(id) {
    if (confirm('Verify: Purge semester cycle and connected syllabus records?')) {
        try {
            await API.deleteSemester(id);
            await loadSemesters();
            showToast('Semester sequence successfully deleted.', 'success');
        } catch (err) {
            showToast('Purge rejected: dependent courses exist.', 'error');
        }
    }
}

async function deleteCourse(id) {
    if (confirm('Verify: Purge course syllabus item?')) {
        try {
            await API.deleteCourse(id);
            await loadCourses();
            showToast('Syllabus course successfully deleted.', 'success');
        } catch (err) {
            showToast('Purge rejected: Active class student enrollments exist.', 'error');
        }
    }
}

async function deleteEnrollment(id) {
    if (confirm('Verify: Revoke class course registration?')) {
        try {
            await API.deleteEnrollment(id);
            await loadEnrollments();
            showToast('Class registration successfully revoked.', 'success');
        } catch (err) {
            showToast('Revoke action rejected.', 'error');
        }
    }
}

// Student GPA Auditor
async function loadStudentGrades() {
    const studentId = document.getElementById('grades-student').value;
    const container = document.getElementById('grades-result');
    if (!studentId) { container.style.display = 'none'; return; }
    try {
        const cgpa = await API.getCGPA(studentId);
        const student = cachedStudents.find(s => s.id == studentId);
        const cgpaVal = cgpa.cgpa || 0;
        let status = 'Academic Warning';
        let bg = 'F';
        if (cgpaVal >= 9.0) { status = 'First Class with Distinction'; bg = 'O'; }
        else if (cgpaVal >= 8.0) { status = 'First Class'; bg = 'A\\+'; }
        else if (cgpaVal >= 7.0) { status = 'Second Class (Upper Division)'; bg = 'A'; }
        else if (cgpaVal >= 6.0) { status = 'Second Class (Lower Division)'; bg = 'B\\+'; }
        else if (cgpaVal >= 5.0) { status = 'Third Class'; bg = 'B'; }
        else if (cgpaVal >= 4.0) { status = 'Pass'; bg = 'C'; }
        container.innerHTML = `
            <div class="stat-card-luxury" style="text-align: left; padding: 2rem;">
                <h4 style="font-family: 'Cormorant Garamond', serif; font-size: 1.6rem; color: var(--crimson); margin-bottom: 1rem;">
                    Academic Status: ${student?.name || 'File'}
                </h4>
                <div class="form-grid" style="grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                    <div>
                        <span class="stat-title-lux">Overall CGPA</span>
                        <div class="stat-value-lux" style="font-size: 2.2rem; color: var(--gold-dark);">${(cgpa.cgpa || 0).toFixed(2)}</div>
                    </div>
                    <div>
                        <span class="stat-title-lux">Academic Standing</span>
                        <div style="margin-top: 0.5rem;">
                            <span class="grade-badge ${bg}">${status}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.style.display = 'block';
    } catch (err) {
        showToast('Failed to pull student audit ledger.', 'error');
        container.style.display = 'none';
    }
}

function logout() {
    localStorage.clear();
    window.location.href = '/';
}

function closeEditModal() {
    const modal = document.getElementById('editModal');
    modal.classList.remove('active');
    document.getElementById('editModalBody').innerHTML = '';
}

function editStudent(id, name, roll, branch, dob) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Student Profile';
    
    const validBranches = cachedDepartments.map(d => d.name);
    const branchOptions = validBranches.map(b => `<option value="${b}" ${b === branch ? 'selected' : ''}>${b}</option>`).join('');
    
    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Full Name</label>
            <input type="text" class="form-input" id="edit-student-name" value="${UI.escapeHTML(name)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic ID / Roll Number</label>
            <input type="text" class="form-input" id="edit-student-roll" value="${UI.escapeHTML(roll)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic Branch</label>
            <select class="form-select" id="edit-student-branch">
                ${branchOptions}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Date of Birth</label>
            <input type="date" class="form-input" id="edit-student-dob" value="${dob}">
        </div>
    `;
    
    modal.dataset.type = 'student';
    modal.dataset.id = id;
    modal.classList.add('active');
}

function editSemester(id, semesterNumber) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Semester Stage';
    
    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Semester Number Sequence</label>
            <input type="number" class="form-input" id="edit-semester-number" value="${semesterNumber}" min="1" max="10">
        </div>
    `;
    
    modal.dataset.type = 'semester';
    modal.dataset.id = id;
    modal.classList.add('active');
}

function editCourse(id, code, name, credits, semesterId, facultyId) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Syllabus Course';
    
    const semOptions = cachedSemesters.map(s => `<option value="${s.id}" ${s.id == semesterId ? 'selected' : ''}>Semester Stage ${s.semesterNumber}</option>`).join('');
    const facOptions = cachedFaculty.map(f => `<option value="${f.id}" ${f.id == facultyId ? 'selected' : ''}>${f.name}</option>`).join('');
    
    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Code</label>
            <input type="text" class="form-input" id="edit-course-code" value="${UI.escapeHTML(code)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Title</label>
            <input type="text" class="form-input" id="edit-course-name" value="${UI.escapeHTML(name)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic Credits Value</label>
            <input type="number" class="form-input" id="edit-course-credits" value="${credits}" min="1" max="6">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Semester Mapping</label>
            <select class="form-select" id="edit-course-semester">
                ${semOptions}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Faculty Instructor</label>
            <select class="form-select" id="edit-course-faculty">
                ${facOptions}
            </select>
        </div>
    `;
    
    modal.dataset.type = 'course';
    modal.dataset.id = id;
    modal.classList.add('active');
}

function editFaculty(id, name, username, email, department) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Faculty Profile';
    
    const validDepts = cachedDepartments.map(d => d.name);
    const deptOptions = validDepts.map(d => `<option value="${d}" ${d === department ? 'selected' : ''}>${d}</option>`).join('');
    
    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Faculty Name</label>
            <input type="text" class="form-input" id="edit-faculty-name" value="${UI.escapeHTML(name)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Username</label>
            <input type="text" class="form-input" id="edit-faculty-username" value="${UI.escapeHTML(username)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Email</label>
            <input type="email" class="form-input" id="edit-faculty-email" value="${UI.escapeHTML(email)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department</label>
            <select class="form-select" id="edit-faculty-department">
                ${deptOptions}
            </select>
        </div>
    `;
    
    modal.dataset.type = 'faculty';
    modal.dataset.id = id;
    modal.classList.add('active');
}

function editDepartment(id, name, code) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Department';
    
    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department Name</label>
            <input type="text" class="form-input" id="edit-department-name" value="${UI.escapeHTML(name)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department Code</label>
            <input type="text" class="form-input" id="edit-department-code" value="${UI.escapeHTML(code)}">
        </div>
    `;
    
    modal.dataset.type = 'department';
    modal.dataset.id = id;
    modal.classList.add('active');
}

async function submitEdit() {
    const modal = document.getElementById('editModal');
    const type = modal.dataset.type;
    const id = modal.dataset.id;
    
    if (type === 'student') {
        const name = document.getElementById('edit-student-name').value.trim();
        const roll = document.getElementById('edit-student-roll').value.trim();
        const branch = document.getElementById('edit-student-branch').value;
        const dob = document.getElementById('edit-student-dob').value;
        
        if (!name || !roll || !branch || !dob) {
            showToast('All student fields are required.', 'error');
            return;
        }
        
        try {
            await API.updateStudent(id, name, roll, branch, dob);
            await loadStudents();
            showToast('Student file successfully modified.', 'success');
            closeEditModal();
        } catch (e) {
            console.error(e);
            showToast('Failed to edit student profile.', 'error');
        }
    } else if (type === 'semester') {
        const numStr = document.getElementById('edit-semester-number').value.trim();
        const num = parseInt(numStr);
        if (isNaN(num) || num < 1 || num > 10) {
            showToast('Semester number must be an integer between 1 and 10.', 'error');
            return;
        }
        try {
            await API.updateSemester(id, num);
            await loadSemesters();
            showToast(`Semester successfully updated to Stage ${num}!`, 'success');
            closeEditModal();
        } catch (err) {
            console.error(err);
            showToast('Failed to modify semester sequence.', 'error');
        }
    } else if (type === 'course') {
        const code = document.getElementById('edit-course-code').value.trim();
        const name = document.getElementById('edit-course-name').value.trim();
        const creditsStr = document.getElementById('edit-course-credits').value.trim();
        const semId = parseInt(document.getElementById('edit-course-semester').value);
        const facId = parseInt(document.getElementById('edit-course-faculty').value);
        
        const credits = parseInt(creditsStr);
        if (isNaN(credits) || credits < 1 || credits > 6) {
            showToast('Credits must be an integer between 1 and 6.', 'error');
            return;
        }
        
        if (!code || !name || isNaN(semId) || isNaN(facId)) {
            showToast('All course fields are required.', 'error');
            return;
        }
        
        try {
            await API.updateCourse(id, code, name, credits, semId, facId);
            await loadCourses();
            showToast(`Syllabus course '${code}' modified successfully!`, 'success');
            closeEditModal();
        } catch (err) {
            console.error(err);
            showToast('Failed to update syllabus course item.', 'error');
        }
    } else if (type === 'faculty') {
        const name = document.getElementById('edit-faculty-name').value.trim();
        const username = document.getElementById('edit-faculty-username').value.trim();
        const email = document.getElementById('edit-faculty-email').value.trim();
        const department = document.getElementById('edit-faculty-department').value;
        
        if (!name || !username || !email || !department) {
            showToast('All faculty fields are required.', 'error');
            return;
        }
        
        try {
            await API.updateFaculty(id, name, username, email, department);
            await loadFaculty();
            showToast(`Faculty profile '${username}' modified successfully!`, 'success');
            closeEditModal();
        } catch (err) {
            console.error(err);
            showToast('Failed to modify faculty record.', 'error');
        }
    } else if (type === 'department') {
        const name = document.getElementById('edit-department-name').value.trim();
        const code = document.getElementById('edit-department-code').value.trim();
        
        if (!name || !code) {
            showToast('Department name and code cannot be empty.', 'error');
            return;
        }
        
        try {
            await API.updateDepartment(id, name, code.toUpperCase());
            await loadDepartments();
            showToast(`Department successfully modified to '${name}'!`, 'success');
            closeEditModal();
        } catch (err) {
            console.error(err);
            showToast('Failed to update department record.', 'error');
        }
    }
}

// Expose functions globally for inline event handlers
window.openPasswordModal = openPasswordModal;
window.closePasswordModal = closePasswordModal;
window.submitPasswordChange = submitPasswordChange;
window.switchTab = switchTab;
window.addStudent = addStudent;
window.addFaculty = addFaculty;
window.addDepartment = addDepartment;
window.addSemester = addSemester;
window.addCourse = addCourse;
window.addEnrollment = addEnrollment;
window.deleteStudent = deleteStudent;
window.deleteFaculty = deleteFaculty;
window.deleteDepartment = deleteDepartment;
window.deleteSemester = deleteSemester;
window.deleteCourse = deleteCourse;
window.deleteEnrollment = deleteEnrollment;
window.loadStudentGrades = loadStudentGrades;
window.editStudent = editStudent;
window.editSemester = editSemester;
window.editCourse = editCourse;
window.editFaculty = editFaculty;
window.editDepartment = editDepartment;
window.closeEditModal = closeEditModal;
window.submitEdit = submitEdit;
window.logout = logout;

// Initialize page data
init();
