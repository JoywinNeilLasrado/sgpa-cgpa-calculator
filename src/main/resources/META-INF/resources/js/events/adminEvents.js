// events/adminEvents.js
import { apiService as API } from '../services/apiService.js';
import { renderStudentList } from '../ui/uiRenderer.js';
import { UI } from '../ui.js';
import { Toast } from '../components/toast.js';
import { Auth } from '../auth/Auth.js';
import { Store } from '../state/store.js';
import { Modal } from '../components/modal.js';
import { handleAPIError } from '../services/errorHandler.js';

function openPasswordModal(username) {
    Modal.open('passwordModal', username);
}

function closePasswordModal() {
    Modal.close('passwordModal');
}

function submitPasswordChange() {
    const username = document.getElementById('passwordModal').dataset.username;
    const newPass = document.getElementById('newPasswordInput').value.trim();
    if (!newPass) {
        Toast.error('Password cannot be empty');
        return;
    }
    API.changeUserPassword(username, newPass).then(() => {
        Toast.success('Password changed successfully');
        closePasswordModal();
    }).catch(err => {
        handleAPIError(err, 'change password');
    });
}



// Check auth state
Auth.requireRole('ADMIN');




function switchTab(tabId, btn) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.sidebar-item').forEach(el => el.classList.remove('active'));

    // Show current
    document.getElementById(`tab-${tabId}`).classList.add('active');
    btn.classList.add('active');
}

async function init() {
    const user = Auth.getUser();
    if (user) {
        const navUsername = document.getElementById('nav-username');
        const navRole = document.getElementById('nav-role');
        if (navUsername) navUsername.textContent = user.username;
        if (navRole) navRole.textContent = user.role;
    }

    setupEventListeners();

    await loadAllData();
    if (window.hidePageLoader) window.hidePageLoader();
}

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.sidebar-item').forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.dataset.tab;
            if (tabId) {
                switchTab(tabId, btn);
            }
        });
    });

    // Create forms
    const btnCreateStudent = document.getElementById('btn-create-student');
    if (btnCreateStudent) btnCreateStudent.addEventListener('click', addStudent);

    const btnCreateSemester = document.getElementById('btn-create-semester');
    if (btnCreateSemester) btnCreateSemester.addEventListener('click', addSemester);

    const btnCreateCourse = document.getElementById('btn-create-course');
    if (btnCreateCourse) btnCreateCourse.addEventListener('click', addCourse);

    const btnCreateEnrollment = document.getElementById('btn-create-enrollment');
    if (btnCreateEnrollment) btnCreateEnrollment.addEventListener('click', addEnrollment);

    const btnCreateFaculty = document.getElementById('btn-create-faculty');
    if (btnCreateFaculty) btnCreateFaculty.addEventListener('click', addFaculty);

    const btnCreateDept = document.getElementById('btn-create-department');
    if (btnCreateDept) btnCreateDept.addEventListener('click', addDepartment);

    // Search input filters
    ['students', 'courses', 'enrollments', 'faculty', 'departments'].forEach(type => {
        const input = document.getElementById(`search-${type}`);
        if (input) {
            input.addEventListener('input', () => filterTable(type));
        }
    });

    // Student grades auditor select
    const gradesSelect = document.getElementById('grades-student');
    if (gradesSelect) gradesSelect.addEventListener('change', loadStudentGrades);

    // Modal close/cancel/submit buttons
    document.querySelectorAll('.close-password-modal-btn, .cancel-password-modal-btn').forEach(btn => {
        btn.addEventListener('click', closePasswordModal);
    });
    const submitPassBtn = document.querySelector('.submit-password-modal-btn');
    if (submitPassBtn) submitPassBtn.addEventListener('click', submitPasswordChange);

    document.querySelectorAll('.close-edit-modal-btn, .cancel-edit-modal-btn').forEach(btn => {
        btn.addEventListener('click', closeEditModal);
    });
    const submitEditBtn = document.querySelector('.submit-edit-modal-btn');
    if (submitEditBtn) submitEditBtn.addEventListener('click', submitEdit);

    // Event Delegation for Tables
    const studentsTbody = document.querySelector('#students-table tbody');
    if (studentsTbody) {
        studentsTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-student-btn');
            const deleteBtn = e.target.closest('.delete-student-btn');
            const passBtn = e.target.closest('.change-pass-student-btn');
            if (editBtn) {
                editStudent(
                    editBtn.dataset.id,
                    editBtn.dataset.name,
                    editBtn.dataset.roll,
                    editBtn.dataset.branch,
                    editBtn.dataset.dob
                );
            } else if (deleteBtn) {
                deleteStudent(deleteBtn.dataset.id);
            } else if (passBtn) {
                openPasswordModal(passBtn.dataset.username);
            }
        });
    }

    const semestersTbody = document.querySelector('#semesters-table tbody');
    if (semestersTbody) {
        semestersTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-semester-btn');
            const deleteBtn = e.target.closest('.delete-semester-btn');
            if (editBtn) {
                editSemester(editBtn.dataset.id, editBtn.dataset.number);
            } else if (deleteBtn) {
                deleteSemester(deleteBtn.dataset.id);
            }
        });
    }

    const coursesTbody = document.querySelector('#courses-table tbody');
    if (coursesTbody) {
        coursesTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-course-btn');
            const deleteBtn = e.target.closest('.delete-course-btn');
            if (editBtn) {
                editCourse(
                    editBtn.dataset.id,
                    editBtn.dataset.code,
                    editBtn.dataset.name,
                    editBtn.dataset.credits,
                    editBtn.dataset.semesterId,
                    editBtn.dataset.facultyId,
                    editBtn.dataset.type
                );
            } else if (deleteBtn) {
                deleteCourse(deleteBtn.dataset.id);
            }
        });
    }

    const enrollmentsTbody = document.querySelector('#enrollments-table tbody');
    if (enrollmentsTbody) {
        enrollmentsTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-enrollment-btn');
            const deleteBtn = e.target.closest('.delete-enrollment-btn');
            if (editBtn) {
                editEnrollment(
                    editBtn.dataset.id,
                    editBtn.dataset.studentId,
                    editBtn.dataset.courseId,
                    editBtn.dataset.cieMarks,
                    editBtn.dataset.cieTheoryMarks,
                    editBtn.dataset.cieLabMarks,
                    editBtn.dataset.seeMarks,
                    editBtn.dataset.graceMarks
                );
            } else if (deleteBtn) {
                deleteEnrollment(deleteBtn.dataset.id);
            }
        });
    }

    const facultyTbody = document.querySelector('#faculty-table tbody');
    if (facultyTbody) {
        facultyTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-faculty-btn');
            const deleteBtn = e.target.closest('.delete-faculty-btn');
            const passBtn = e.target.closest('.change-pass-faculty-btn');
            if (editBtn) {
                editFaculty(
                    editBtn.dataset.id,
                    editBtn.dataset.name,
                    editBtn.dataset.username,
                    editBtn.dataset.email,
                    editBtn.dataset.department
                );
            } else if (deleteBtn) {
                deleteFaculty(deleteBtn.dataset.id);
            } else if (passBtn) {
                openPasswordModal(passBtn.dataset.username);
            }
        });
    }

    const deptsTbody = document.querySelector('#departments-table tbody');
    if (deptsTbody) {
        deptsTbody.addEventListener('click', (e) => {
            const editBtn = e.target.closest('.edit-department-btn');
            const deleteBtn = e.target.closest('.delete-department-btn');
            if (editBtn) {
                editDepartment(editBtn.dataset.id, editBtn.dataset.name, editBtn.dataset.code);
            } else if (deleteBtn) {
                deleteDepartment(deleteBtn.dataset.id);
            }
        });
    }
}

async function loadAllData() {
    await loadDepartments();
    await loadStudents();
    await loadSemesters();
    await loadCourses();
    await loadFaculty();
    await loadEnrollments();

    // Sync summary counters
    document.getElementById('count-students').textContent = (Store.get('students') || []).length;
    document.getElementById('count-courses').textContent = (Store.get('courses') || []).length;
    document.getElementById('count-enrollments').textContent = (Store.get('enrollments') || []).length;
}

async function loadStudents() {
    try {
        const students = await API.getStudents();
        
        Store.set('students', students);

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
        Toast.error('Failed to pull student accounts.');
    }
}

async function loadSemesters() {
    try {
        const semesters = await API.getSemesters();
        Store.set('semesters', semesters);
        document.getElementById('count-semesters').textContent = semesters.length;

        const tbody = document.querySelector('#semesters-table tbody');
        tbody.innerHTML = semesters.length ? semesters.map(s => `
            <tr>
                <td><strong>${s.id}</strong></td>
                <td>Semester Stage ${s.semesterNumber}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-semester-btn" style="margin-right: 0.5rem;" data-id="${s.id}" data-number="${s.semesterNumber}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-semester-btn" data-id="${s.id}">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="3" class="empty-state">No semester cycles defined.</td></tr>';

        // Populate semester dropdown
        const select = document.getElementById('course-semester');
        select.innerHTML = semesters.map(s => `<option value="${s.id}">Semester Stage ${s.semesterNumber}</option>`).join('');

    } catch (err) {
        console.error(err);
        Toast.error('Failed to load semesters.');
    }
}

async function loadCourses() {
    try {
        const courses = await API.getCourses();
        
        Store.set('courses', courses);

        const tbody = document.querySelector('#courses-table tbody');
        tbody.innerHTML = courses.length ? courses.map(c => `
            <tr class="course-row-item" data-code="${c.code.toLowerCase()}" data-name="${c.name.toLowerCase()}">
                <td><strong>${c.code}</strong></td>
                <td>${c.name}</td>
                <td>${c.credits} Credits<br><small style="color:var(--slate-light)">${c.courseType || 'THEORY'}</small></td>
                <td><span class="grade-badge O">Sem ${c.semester?.semesterNumber || '-'}</span></td>
                <td>${c.faculty?.name || '-'}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-course-btn" style="margin-right: 0.5rem;" 
                        data-id="${c.id}" 
                        data-code="${UI.escapeHTML(c.code)}" 
                        data-name="${UI.escapeHTML(c.name)}" 
                        data-credits="${c.credits}" 
                        data-semester-id="${c.semester?.id || 0}" 
                        data-faculty-id="${c.faculty?.id || 0}" 
                        data-type="${c.courseType || 'THEORY'}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-course-btn" data-id="${c.id}">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="6" class="empty-state">Curriculum inventory is empty.</td></tr>';

        // Populate enroll course dropdown
        const select = document.getElementById('enrollment-course');
        select.innerHTML = '<option value="">Select syllabus course...</option>' +
            courses.map(c => `<option value="${c.id}">${c.code} - ${c.name}</option>`).join('');

    } catch (err) {
        console.error(err);
        Toast.error('Failed to pull curriculum list.');
    }
}

async function loadFaculty() {
    try {
        const faculty = await API.getFacultyMembers();
        Store.set('faculty', faculty);
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
                    <button class="btn btn-secondary btn-sm edit-faculty-btn" style="margin-right: 0.5rem;" 
                        data-id="${f.id}" 
                        data-name="${UI.escapeHTML(f.name)}" 
                        data-username="${UI.escapeHTML(f.username)}" 
                        data-email="${UI.escapeHTML(f.email || '')}" 
                        data-department="${UI.escapeHTML(f.department || '')}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-faculty-btn" style="margin-right: 0.5rem;" data-id="${f.id}">Delete</button>
                    <button class="btn btn-warning btn-sm change-pass-faculty-btn" data-username="${UI.escapeHTML(f.username)}">Change Password</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="7" class="empty-state">No faculty accounts registered.</td></tr>';
        document.getElementById('count-faculty-text').textContent = `${faculty.length} faculty members loaded`;
    } catch (err) {
        console.error(err);
        Toast.error('Failed to load faculty list.');
    }
}

async function loadDepartments() {
    try {
        const departments = await API.getDepartments();
        Store.set('departments', departments);

        document.getElementById('count-departments-text').textContent = `${departments.length} departments loaded`;

        const tbody = document.querySelector('#departments-table tbody');
        tbody.innerHTML = departments.length ? departments.map(d => `
            <tr class="department-row-item" data-name="${d.name.toLowerCase()}" data-code="${d.code.toLowerCase()}">
                <td><strong>${d.id}</strong></td>
                <td>${d.name}</td>
                <td><span class="grade-badge A">${d.code}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-department-btn" style="margin-right: 0.5rem;" 
                        data-id="${d.id}" 
                        data-name="${UI.escapeHTML(d.name)}" 
                        data-code="${UI.escapeHTML(d.code)}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-department-btn" data-id="${d.id}">Delete</button>
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
        Toast.error('Failed to pull departments.');
    }
}

async function loadEnrollments() {
    try {
        const enrollments = await API.getEnrollments();
        
        Store.set('enrollments', enrollments);

        const tbody = document.querySelector('#enrollments-table tbody');
        tbody.innerHTML = enrollments.length ? enrollments.map(e => `
            <tr class="enroll-row-item" data-student="${(e.studentName || '').toLowerCase()}" data-code="${(e.courseCode || '').toLowerCase()}">
                <td><strong>${e.studentName || 'N/A'}</strong></td>
                <td>${e.courseCode || 'N/A'} - ${e.courseName || 'N/A'}</td>
                <td><span class="grade-badge ${e.grade || 'none'}">${e.grade || 'Pending'}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-enrollment-btn" style="margin-right: 0.5rem;" 
                        data-id="${e.id}" 
                        data-student-id="${e.studentId || 0}" 
                        data-course-id="${e.courseId || 0}" 
                        data-cie-marks="${e.cieMarks || 0}" 
                        data-cie-theory-marks="${e.cieTheoryMarks || 0}" 
                        data-cie-lab-marks="${e.cieLabMarks || 0}" 
                        data-see-marks="${e.seeMarks || 0}" 
                        data-grace-marks="${e.graceMarks || 0}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-enrollment-btn" data-id="${e.id}">Delete</button>
                </td>
            </tr>
        `).join('') : '<tr><td colspan="4" class="empty-state">No course registrations active.</td></tr>';

    } catch (err) {
        console.error(err);
        Toast.error('Failed to load system registrations.');
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
    if (!name || !studentId || !dob) { Toast.error('Complete all floating fields.'); return; }
    try {
        await API.createStudent(name, studentId, branch, dob);
        document.getElementById('student-name').value = '';
        document.getElementById('student-id').value = '';
        document.getElementById('student-dob').value = '2004-01-01';
        await loadStudents();
        Toast.success(`Student profile '${name}' registered successfully!`);
    } catch (err) {
        console.error(err);
        Toast.error('Failed to register student record.');
    }
}

async function addFaculty() {
    const name = document.getElementById('faculty-name').value.trim();
    const username = document.getElementById('faculty-username').value.trim();
    const email = document.getElementById('faculty-email').value.trim();
    const department = document.getElementById('faculty-department').value.trim();
    if (!name || !username || !email || !department) { Toast.error('Complete all faculty fields.'); return; }
    try {
        const password = prompt('Enter a password for the new faculty member:', 'password123');
        if (password === null) return;
        if (!password.trim()) { Toast.error('Password cannot be empty.'); return; }
        await API.createFaculty(name, username, password.trim(), email, department);
        document.getElementById('faculty-name').value = '';
        document.getElementById('faculty-username').value = '';
        document.getElementById('faculty-email').value = '';
        document.getElementById('faculty-department').value = '';
        await loadFaculty();
        Toast.success(`Faculty staff '${name}' onboarded successfully!`);
    } catch (err) {
        console.error(err);
        Toast.error('Failed to onboard faculty staff.');
    }
}

async function addDepartment() {
    const name = document.getElementById('department-name').value.trim();
    const code = document.getElementById('department-code').value.trim();
    if (!name || !code) { Toast.error('Complete all department fields.'); return; }
    try {
        await API.createDepartment(name, code);
        document.getElementById('department-name').value = '';
        document.getElementById('department-code').value = '';
        await loadDepartments();
        Toast.success(`Department '${name}' created successfully!`);
    } catch (err) {
        console.error(err);
        Toast.error('Failed to create department. Duplicates?');
    }
}

async function addSemester() {
    const num = document.getElementById('semester-number').value.trim();
    if (!num) { Toast.error('Provide a semester number cycle.'); return; }
    try {
        await API.createSemester(parseInt(num));
        document.getElementById('semester-number').value = '';
        await loadSemesters();
        Toast.success(`Academic Semester Stage ${num} established!`);
    } catch (err) {
        console.error(err);
        Toast.error('Failed to establish semester stage.');
    }
}

async function addCourse() {
    const code = document.getElementById('course-code').value.trim();
    const name = document.getElementById('course-name').value.trim();
    const credits = document.getElementById('course-credits').value.trim();
    const semesterId = document.getElementById('course-semester').value;
    const facultyId = document.getElementById('course-faculty').value;
    const courseType = document.getElementById('course-type').value;
    if (!code || !name || !credits || !semesterId || !facultyId) { Toast.error('Complete all course fields.'); return; }
    try {
        await API.createCourse(code, name, parseInt(credits), semesterId, facultyId, courseType);
        document.getElementById('course-code').value = '';
        document.getElementById('course-name').value = '';
        document.getElementById('course-credits').value = '';
        await loadCourses();
        Toast.success(`Syllabus item '${code}: ${name}' created successfully.`);
    } catch (err) {
        console.error(err);
        Toast.error('Failed to create syllabus course item.');
    }
}

async function addEnrollment() {
    const studentId = document.getElementById('enrollment-student').value;
    const courseId = document.getElementById('enrollment-course').value;
    if (!studentId || !courseId) { Toast.error('Choose student target and course mapping.'); return; }
    try {
        await API.createEnrollment(studentId, courseId);
        await loadEnrollments();
        Toast.success('Course registration siphoned successfully.');
    } catch (err) {
        console.error(err);
        Toast.error('Failed to map student registration. Duplicates?');
    }
}

// Destructive actions
async function deleteStudent(id) {
    if (confirm('Verify: Permanently purge student record and related grades files?')) {
        try {
            await API.deleteStudent(id);
            await loadStudents();
            Toast.success('Student file successfully purged.');
        } catch (err) {
            Toast.error('Purge rejected by data locks.');
        }
    }
}

async function deleteFaculty(id) {
    if (confirm('Verify: Permanently purge faculty member account?')) {
        try {
            await API.deleteFaculty(id);
            await loadFaculty();
            Toast.success('Faculty member successfully purged.');
        } catch (err) {
            console.error(err);
            Toast.error('Failed to delete faculty member.');
        }
    }
}

async function deleteDepartment(id) {
    if (confirm('Verify: Permanently delete this department?')) {
        try {
            await API.deleteDepartment(id);
            await loadDepartments();
            Toast.success('Department successfully deleted.');
        } catch (err) {
            Toast.error('Failed to delete department.');
        }
    }
}

async function deleteSemester(id) {
    if (confirm('Verify: Purge semester cycle and connected syllabus records?')) {
        try {
            await API.deleteSemester(id);
            await loadSemesters();
            Toast.success('Semester sequence successfully deleted.');
        } catch (err) {
            Toast.error('Purge rejected: dependent courses exist.');
        }
    }
}

async function deleteCourse(id) {
    if (confirm('Verify: Purge course syllabus item?')) {
        try {
            await API.deleteCourse(id);
            await loadCourses();
            Toast.success('Syllabus course successfully deleted.');
        } catch (err) {
            Toast.error('Purge rejected: Active class student enrollments exist.');
        }
    }
}

async function deleteEnrollment(id) {
    if (confirm('Verify: Revoke class course registration?')) {
        try {
            await API.deleteEnrollment(id);
            await loadEnrollments();
            Toast.success('Class registration successfully revoked.');
        } catch (err) {
            Toast.error('Revoke action rejected.');
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
        const student = (Store.get('students') || []).find(s => s.id == studentId);
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
        Toast.error('Failed to pull student audit ledger.');
        container.style.display = 'none';
    }
}

function logout() {
    Auth.logout();
}

function closeEditModal() {
    Modal.close('editModal');
}

function editStudent(id, name, roll, branch, dob) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Student Profile';
    
    const validBranches = (Store.get('departments') || []).map(d => d.name);
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

function editCourse(id, code, name, credits, semesterId, facultyId, courseType) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Syllabus Course';
    
    const semOptions = (Store.get('semesters') || []).map(s => `<option value="${s.id}" ${s.id == semesterId ? 'selected' : ''}>Semester Stage ${s.semesterNumber}</option>`).join('');
    const facOptions = (Store.get('faculty') || []).map(f => `<option value="${f.id}" ${f.id == facultyId ? 'selected' : ''}>${f.name}</option>`).join('');
    
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
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Type</label>
            <select class="form-select" id="edit-course-type">
                <option value="THEORY" ${courseType==='THEORY'?'selected':''}>Theory</option>
                <option value="LABORATORY" ${courseType==='LABORATORY'?'selected':''}>Laboratory</option>
                <option value="INTEGRATED" ${courseType==='INTEGRATED'?'selected':''}>Integrated (Theory + Lab)</option>
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
    
    const validDepts = (Store.get('departments') || []).map(d => d.name);
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

function editEnrollment(id, studentId, courseId, cieMarks, cieTheoryMarks, cieLabMarks, seeMarks, graceMarks) {
    const modal = document.getElementById('editModal');
    document.getElementById('editModalTitle').textContent = 'Edit Course Enrollment';
    
    const studentOptions = (Store.get('students') || []).map(s => `<option value="${s.id}" ${s.id == studentId ? 'selected' : ''}>${s.name} (${s.studentId})</option>`).join('');
    const courses = Store.get('courses') || [];
    const courseOptions = courses.map(c => `<option value="${c.id}" ${c.id == courseId ? 'selected' : ''}>${c.code} - ${c.name}</option>`).join('');
    
    const course = (Store.get('courses') || []).find(c => c.id == courseId);
    const courseType = course ? (course.courseType || 'THEORY') : 'THEORY';

    let marksHtml = '';
    if (courseType === 'INTEGRATED') {
        marksHtml = `
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Theory Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie-theory" value="${cieTheoryMarks}" min="0" max="50">
            </div>
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Lab Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie-lab" value="${cieLabMarks}" min="0" max="50">
            </div>
        `;
    } else {
        marksHtml = `
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie" value="${cieMarks}" min="0" max="50">
            </div>
        `;
    }

    marksHtml += `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">SEE Marks</label>
            <input type="number" class="form-input" id="edit-enrollment-see" value="${seeMarks}" min="0" max="50">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Grace Marks</label>
            <input type="number" class="form-input" id="edit-enrollment-grace" value="${graceMarks}" min="0" max="5">
        </div>
    `;

    document.getElementById('editModalBody').innerHTML = `
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Select Student Record</label>
            <select class="form-select" id="edit-enrollment-student" disabled>
                ${studentOptions}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Select Syllabus Course</label>
            <select class="form-select" id="edit-enrollment-course" disabled>
                ${courseOptions}
            </select>
        </div>
        ${marksHtml}
    `;
    
    modal.dataset.type = 'enrollment';
    modal.dataset.id = id;
    modal.dataset.courseType = courseType;
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
        const courseType = document.getElementById('edit-course-type').value;
        
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
            await API.updateCourse(id, code, name, credits, semId, facId, courseType);
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
    } else if (type === 'enrollment') {
        const studentId = parseInt(document.getElementById('edit-enrollment-student').value);
        const courseId = parseInt(document.getElementById('edit-enrollment-course').value);
        
        if (isNaN(studentId) || isNaN(courseId)) {
            showToast('Student and Course selections are required.', 'error');
            return;
        }
        
        const courseType = modal.dataset.courseType;
        
        let cieMarks = 0, cieTheoryMarks = 0, cieLabMarks = 0;
        if (courseType === 'INTEGRATED') {
            cieTheoryMarks = parseInt(document.getElementById('edit-enrollment-cie-theory').value) || 0;
            cieLabMarks = parseInt(document.getElementById('edit-enrollment-cie-lab').value) || 0;
        } else {
            cieMarks = parseInt(document.getElementById('edit-enrollment-cie').value) || 0;
        }
        
        const seeMarks = parseInt(document.getElementById('edit-enrollment-see').value) || 0;
        const graceMarks = parseInt(document.getElementById('edit-enrollment-grace').value) || 0;
        
        try {
            await API.updateEnrollment(id, studentId, courseId, cieMarks, cieTheoryMarks, cieLabMarks, seeMarks, graceMarks);
            await loadEnrollments();
            showToast('Course enrollment successfully updated!', 'success');
            closeEditModal();
        } catch (err) {
            console.error(err);
            showToast('Failed to update course enrollment marks.', 'error');
        }
    }
}

// Initialize page data
init();
