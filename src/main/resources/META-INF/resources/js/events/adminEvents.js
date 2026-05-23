// events/adminEvents.js
import { apiService as API } from '../services/apiService.js';
import { renderStudentList } from '../ui/uiRenderer.js';

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
        document.getElementById('count-semesters').textContent = semesters.length;

        const tbody = document.querySelector('#semesters-table tbody');
        tbody.innerHTML = semesters.length ? semesters.map(s => `
            <tr>
                <td><strong>${s.id}</strong></td>
                <td>Semester Stage ${s.semesterNumber}</td>
                <td style="text-align: right;">
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
                <td>${f.email || '-'}</td>
                <td>${f.department || '-'}</td>
                <td style="text-align: right;">
                    <button class="btn btn-danger btn-sm" onclick="deleteFaculty(${f.id})">Delete</button>
                    <button class="btn btn-warning btn-sm" onclick="openPasswordModal('${f.username}')">Change Password</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="6" class="empty-state">No faculty accounts registered.</td></tr>';
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
    if (!name || !studentId) { showToast('Complete all floating fields.', 'error'); return; }
    try {
        await API.createStudent(name, studentId, branch);
        document.getElementById('student-name').value = '';
        document.getElementById('student-id').value = '';
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

async function editStudent(id, oldName, oldRoll, oldBranch) {
    const newName = prompt('Modify student name:', oldName);
    if (newName === null) return;
    const newRoll = prompt('Modify student Roll ID:', oldRoll);
    if (newRoll === null) return;
    const validBranches = cachedDepartments.map(d => d.name);
    const branchOptionsStr = validBranches.join(' / ');
    const newBranch = prompt(`Modify student Academic Branch (${branchOptionsStr}):`, oldBranch || 'Computer Science');
    if (newBranch === null) return;
    if (!newName.trim() || !newRoll.trim() || !newBranch.trim()) {
        showToast('Invalid edits. Inputs cannot be empty.', 'error');
        return;
    }
    const trimmedBranch = newBranch.trim();
    if (!validBranches.includes(trimmedBranch)) {
        showToast(`Invalid branch. Must be one of: ${validBranches.join(', ')}`, 'error');
        return;
    }
    try {
        await API.updateStudent(id, newName.trim(), newRoll.trim(), trimmedBranch);
        await loadStudents();
        showToast('Student file successfully modified.', 'success');
    } catch(e) {
        showToast('Failed to edit student profile.', 'error');
    }
}

function logout() {
    localStorage.clear();
    window.location.href = '/';
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
window.logout = logout;

// Initialize page data
init();
