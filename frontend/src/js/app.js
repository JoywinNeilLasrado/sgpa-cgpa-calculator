/**
 * GradePoint Main Application
 * Initializes the app and coordinates modules
 */

(function() {
    'use strict';

    // Global app state
    let App = {
        currentStudent: null,
        currentSemester: null,
        data: {
            students: [],
            semesters: [],
            courses: [],
            enrollments: []
        }
    };

    // Initialize application
    async function init() {
        console.log('Initializing GradePoint...');
        
        // Load initial data
        await loadAllData();
        
        // Setup event listeners
        setupTabs();
        setupForms();
        setupFilters();
        
        // Initial render
        renderTables();
        populateDropdowns();
        
        console.log('GradePoint initialized!');
    }

    // Load all data from API
    async function loadAllData() {
        try {
            App.data.students = await API.getStudents();
            App.data.semesters = await API.getSemesters();
            App.data.courses = await API.getCourses();
            App.data.enrollments = await API.getEnrollments();
        } catch (err) {
            console.error('Failed to load data:', err);
            UI.showError('Failed to load data');
        }
    }

    // Setup tab navigation
    function setupTabs() {
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTab = btn.dataset.tab;

                // Update buttons
                tabButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                // Update content
                tabContents.forEach(content => {
                    content.style.display = content.id === targetTab + '-section' ? '' : 'none';
                });
            });
        });
    }

    // Setup form handlers
    function setupForms() {
        // Student form
        const studentForm = document.getElementById('student-form');
        if (studentForm) {
            studentForm.addEventListener('submit', handleCreateStudent);
        }

        // Course form
        const courseForm = document.getElementById('course-form');
        if (courseForm) {
            courseForm.addEventListener('submit', handleCreateCourse);
        }

        // Enrollment form
        const enrollForm = document.getElementById('enrollment-form');
        if (enrollForm) {
            enrollForm.addEventListener('submit', handleCreateEnrollment);
        }
    }

    // Setup filter inputs
    function setupFilters() {
        const filters = ['students-filter', 'semesters-filter', 'courses-filter', 'enrollments-filter'];
        
        filters.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('input', UI.debounce(() => renderFilteredTable(id), 300));
            }
        });
    }

    // Render filtered table
    function renderFilteredTable(filterId) {
        const tableId = filterId.replace('-filter', '-table');
        UI.filterTable(tableId, filterId);
    }

    // Populate dropdowns
    function populateDropdowns() {
        // Student dropdowns
        UI.populateSelect('sgpa-student', App.data.students, 'id', 'name');
        UI.populateSelect('cgpa-student', App.data.students, 'id', 'name');
        UI.populateSelect('dashboard-student', App.data.students, 'id', 'name');
        UI.populateSelect('student-dropdown', App.data.students, 'id', 'name');
        
        // Semester dropdowns
        UI.populateSelect('sgpa-semester', App.data.semesters, 'id', 'semesterNumber');
        UI.populateSelect('course-semester', App.data.semesters, 'id', 'semesterNumber');
        
        // Course dropdowns
        UI.populateSelect('enrollment-course', App.data.courses, 'id', 'courseCode');
    }

    // Render all tables
    function renderTables() {
        renderStudentsTable();
        renderSemestersTable();
        renderCoursesTable();
        renderEnrollmentsTable();
        renderGradeTable();
    }

    // Render students table
    function renderStudentsTable() {
        const tbody = document.getElementById('students-table');
        if (!tbody) return;

        tbody.innerHTML = App.data.students.map(s => `
            <tr>
                <td>${s.name}</td>
                <td>${s.studentId}</td>
                <td>
                    <button class="action-btn" onclick="App.deleteStudent(${s.id})">Delete</button>
                </td>
            </tr>
        `).join('');
    }

    // Render semesters table
    function renderSemestersTable() {
        const tbody = document.getElementById('semesters-table');
        if (!tbody) return;

        tbody.innerHTML = App.data.semesters.map(s => `
            <tr>
                <td>Semester ${s.semesterNumber}</td>
                <td>
                    <button class="action-btn" onclick="App.deleteSemester(${s.id})">Delete</button>
                </td>
            </tr>
        `).join('');
    }

    // Render courses table
    function renderCoursesTable() {
        const tbody = document.getElementById('courses-table');
        if (!tbody) return;

        tbody.innerHTML = App.data.courses.map(c => `
            <tr>
                <td>${c.courseCode}</td>
                <td>${c.courseName}</td>
                <td>${c.credits}</td>
                <td>${c.semester?.semesterNumber || '-'}</td>
                <td>
                    <button class="action-btn" onclick="App.deleteCourse(${c.id})">Delete</button>
                </td>
            </tr>
        `).join('');
    }

    // Render enrollments table
    function renderEnrollmentsTable() {
        const tbody = document.getElementById('enrollments-table');
        if (!tbody) return;

        tbody.innerHTML = App.data.enrollments.map(e => `
            <tr>
                <td>${e.student?.name || '-'}</td>
                <td>${e.course?.courseName || '-'}</td>
                <td>${e.course?.semester?.semesterNumber || '-'}</td>
                <td>${e.grade?.grade || '-'}</td>
                <td>${e.creditPoints || '-'}</td>
                <td>
                    <button class="action-btn" onclick="App.deleteEnrollment(${e.id})">Delete</button>
                </td>
            </tr>
        `).join('');
    }

    // Render grade scale table
    function renderGradeTable() {
        const tbody = document.getElementById('grade-table-body');
        if (!tbody) return;

        const grades = [
            { grade: 'O', name: 'Outstanding', range: '90-100', points: 10 },
            { grade: 'A+', name: 'Excellent', range: '80-89', points: 9 },
            { grade: 'A', name: 'Very Good', range: '70-79', points: 8 },
            { grade: 'B+', name: 'Good', range: '60-69', points: 7 },
            { grade: 'B', name: 'Above Average', range: '55-59', points: 6 },
            { grade: 'C', name: 'Average', range: '50-54', points: 5 },
            { grade: 'P', name: 'Pass', range: '40-49', points: 4 },
            { grade: 'F', name: 'Fail', range: '00-39', points: 0 }
        ];

        tbody.innerHTML = grades.map(g => `
            <tr>
                <td><strong>${g.grade}</strong></td>
                <td>${g.name}</td>
                <td>${g.range}</td>
                <td><strong>${g.points}</strong></td>
            </tr>
        `).join('');
    }

    // ===== Event Handlers =====

    async function handleCreateStudent(e) {
        e.preventDefault();
        
        const name = document.getElementById('student-name')?.value;
        const roll = document.getElementById('student-roll')?.value;
        
        if (!name || !roll) {
            UI.showError('Please fill all fields');
            return;
        }

        try {
            await API.createStudent({ name, studentId: roll });
            UI.showSuccess('Student created!');
            App.data.students = await API.getStudents();
            renderTables();
            populateDropdowns();
            
            // Clear form
            e.target.reset();
        } catch (err) {
            UI.showError('Failed to create student');
        }
    }

    async function handleCreateCourse(e) {
        e.preventDefault();
        
        const code = document.getElementById('course-code')?.value;
        const name = document.getElementById('course-name')?.value;
        const credits = parseInt(document.getElementById('course-credits')?.value);
        const semesterId = parseInt(document.getElementById('course-semester')?.value);
        
        if (!code || !name || !credits || !semesterId) {
            UI.showError('Please fill all fields');
            return;
        }

        try {
            await API.createCourse({ courseCode: code, courseName: name, credits, semesterId });
            UI.showSuccess('Course created!');
            App.data.courses = await API.getCourses();
            renderTables();
            
            e.target.reset();
        } catch (err) {
            UI.showError('Failed to create course');
        }
    }

    async function handleCreateEnrollment(e) {
        e.preventDefault();
        
        const studentId = parseInt(document.getElementById('enrollment-student')?.value);
        const courseId = parseInt(document.getElementById('enrollment-course')?.value);
        const grade = document.getElementById('enrollment-grade')?.value;
        
        if (!studentId || !courseId || !grade) {
            UI.showError('Please fill all fields');
            return;
        }

        try {
            await API.createEnrollment({ studentId, courseId, grade });
            UI.showSuccess('Enrollment created!');
            App.data.enrollments = await API.getEnrollments();
            renderTables();
            
            e.target.reset();
        } catch (err) {
            UI.showError('Failed to create enrollment');
        }
    }

    // ===== Public Methods =====

    async function deleteStudent(id) {
        if (!confirm('Delete this student?')) return;
        
        try {
            await API.deleteStudent(id);
            UI.showSuccess('Student deleted!');
            App.data.students = await API.getStudents();
            renderTables();
            populateDropdowns();
        } catch (err) {
            UI.showError('Failed to delete');
        }
    }

    async function deleteSemester(id) {
        if (!confirm('Delete this semester?')) return;
        
        try {
            await API.deleteSemester(id);
            UI.showSuccess('Semester deleted!');
            App.data.semesters = await API.getSemesters();
            renderTables();
            populateDropdowns();
        } catch (err) {
            UI.showError('Failed to delete');
        }
    }

    async function deleteCourse(id) {
        if (!confirm('Delete this course?')) return;
        
        try {
            await API.deleteCourse(id);
            UI.showSuccess('Course deleted!');
            App.data.courses = await API.getCourses();
            renderTables();
        } catch (err) {
            UI.showError('Failed to delete');
        }
    }

    async function deleteEnrollment(id) {
        if (!confirm('Delete this enrollment?')) return;
        
        try {
            await API.deleteEnrollment(id);
            UI.showSuccess('Enrollment deleted!');
            App.data.enrollments = await API.getEnrollments();
            renderTables();
        } catch (err) {
            UI.showError('Failed to delete');
        }
    }

    // Calculate SGPA
    async function calcSGPA() {
        const studentId = document.getElementById('sgpa-student')?.value;
        const semesterId = document.getElementById('sgpa-semester')?.value;
        
        if (!studentId || !semesterId) {
            UI.showError('Select student and semester');
            return;
        }

        try {
            const result = await API.calculateSGPA(studentId, semesterId);
            document.getElementById('sgpa-result').textContent = Calculator.formatGPA(result.sgpa);
        } catch (err) {
            UI.showError('Failed to calculate');
        }
    }

    // Calculate CGPA
    async function calcCGPA() {
        const studentId = document.getElementById('cgpa-student')?.value;
        
        if (!studentId) {
            UI.showError('Select a student');
            return;
        }

        try {
            const result = await API.calculateCGPA(studentId);
            document.getElementById('cgpa-result').textContent = Calculator.formatGPA(result.cgpa);
        } catch (err) {
            UI.showError('Failed to calculate');
        }
    }

    // ===== Export to window =====

    window.App = App;
    window.App.init = init;
    window.App.deleteStudent = deleteStudent;
    window.App.deleteSemester = deleteSemester;
    window.App.deleteCourse = deleteCourse;
    window.App.deleteEnrollment = deleteEnrollment;
    window.App.calcSGPA = calcSGPA;
    window.App.calcCGPA = calcCGPA;
    window.App.renderTables = renderTables;

    // Auto-initialize when DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();