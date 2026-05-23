/**
 * GradePoint API Wrapper v2
 * Complete API communication layer with JWT auth
 */

const API = (() => {
    const BASE_URL = '/api';

    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return '';
    }

    function getHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
        };
    }

    async function request(endpoint, options = {}) {
        const url = `${BASE_URL}${endpoint}`;
        const headers = getHeaders();
        
        // Attach CSRF token for mutating requests (POST, PUT, DELETE)
        const method = (options.method || 'GET').toUpperCase();
        if (method !== 'GET' && method !== 'HEAD') {
            const csrfToken = getCookie('XSRF-TOKEN');
            if (csrfToken) {
                headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        const config = {
            headers,
            ...options
        };

        const res = await fetch(url, config);
        if (res.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
            return;
        }
        if (!res.ok) {
            let errorMsg = `API Error: ${res.statusText}`;
            try {
                const errData = JSON.parse(await res.text());
                if (errData && errData.message) {
                    errorMsg = errData.message;
                }
            } catch (e) {
                // If it's not JSON, fallback to standard error msg
            }
            throw new Error(errorMsg);
        }
        const text = await res.text();
        return text ? JSON.parse(text) : {};
    }

    return {
        // Auth
        login: (username, password) => request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        }),

        getCurrentUser: () => request('/auth/me', { method: 'GET' }),

        changePassword: (oldPassword, newPassword) => request('/auth/change-password', {
            method: 'PUT',
            body: JSON.stringify({ oldPassword, newPassword })
        }),

        changeUserPassword: (username, newPassword) => request('/admin/users/password', {
            method: 'PUT',
            body: JSON.stringify({ username, newPassword })
        }),

        // Students
        getStudents: () => request('/students', { method: 'GET' }),
        getStudent: (id) => request(`/students/${id}`, { method: 'GET' }),
        getStudentDashboard: (id) => request(`/students/${id}/dashboard`, { method: 'GET' }),
        createStudent: (name, studentId, branch, dateOfBirth) => request('/students', {
            method: 'POST',
            body: JSON.stringify({ name, studentId, branch, dateOfBirth })
        }),
        updateStudent: (id, name, studentId, branch, dateOfBirth) => request(`/students/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ name, studentId, branch, dateOfBirth })
        }),
        deleteStudent: (id) => request(`/students/${id}`, { method: 'DELETE' }),

        // Semesters
        getSemesters: () => request('/semesters', { method: 'GET' }),
        getSemester: (id) => request(`/semesters/${id}`, { method: 'GET' }),
        createSemester: (semesterNumber) => request('/semesters', {
            method: 'POST',
            body: JSON.stringify({ semesterNumber })
        }),
        updateSemester: (id, semesterNumber) => request(`/semesters/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ semesterNumber })
        }),
        deleteSemester: (id) => request(`/semesters/${id}`, { method: 'DELETE' }),

        // Courses
        getCourses: () => request('/courses', { method: 'GET' }),
        getCoursesBySemester: (semesterId) => request(`/courses/semester/${semesterId}`, { method: 'GET' }),
        getCourse: (id) => request(`/courses/${id}`, { method: 'GET' }),
        createCourse: (courseCode, courseName, credits, semesterId, facultyId) => request('/courses', {
            method: 'POST',
            body: JSON.stringify({ courseCode, courseName, credits, semesterId, facultyId })
        }),
        updateCourse: (id, courseCode, courseName, credits, semesterId, facultyId) => request(`/courses/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ courseCode, courseName, credits, semesterId, facultyId })
        }),
        deleteCourse: (id) => request(`/courses/${id}`, { method: 'DELETE' }),

        // Enrollments
        getEnrollments: () => request('/enrollments', { method: 'GET' }),
        getEnrollmentsByStudent: (studentId) => request(`/enrollments/student/${studentId}`, { method: 'GET' }),
        getEnrollmentsByStudentSemester: (studentId, semesterId) => 
            request(`/enrollments/student/${studentId}/semester/${semesterId}`, { method: 'GET' }),
        createEnrollment: (studentId, courseId) => request('/enrollments', {
            method: 'POST',
            body: JSON.stringify({ studentId, courseId })
        }),
        updateEnrollment: (id, studentId, courseId, grade) => request(`/enrollments/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ studentId, courseId, grade })
        }),
        deleteEnrollment: (id) => request(`/enrollments/${id}`, { method: 'DELETE' }),

        // Grades & SGPA/CGPA
        getSGPA: (studentId, semesterId) => 
            request(`/sgpa/student/${studentId}/semester/${semesterId}`, { method: 'GET' }),
        getCGPA: (studentId, semesterId) => 
            semesterId 
                ? request(`/cgpa/student/${studentId}/semester/${semesterId}`, { method: 'GET' })
                : request(`/cgpa/student/${studentId}`, { method: 'GET' }),
        getGradeScale: () => request('/grade-scale', { method: 'GET' }),
        getGradeFromMarks: (marks) => request(`/grades/from-marks?marks=${marks}`, { method: 'GET' }),

        // Faculty APIs
        getFacultyMembers: () => request('/faculty/members', { method: 'GET' }),
        createFaculty: (name, username, password, email, department) =>
            request('/faculty/register', {
                method: 'POST',
                body: JSON.stringify({ name, username, password, email, department })
            }),
        updateFaculty: (id, name, username, email, department) =>
            request(`/faculty/members/${id}`, {
                method: 'PUT',
                body: JSON.stringify({ name, username, email, department })
            }),
        deleteFaculty: (id) => request(`/faculty/members/${id}`, { method: 'DELETE' }),

        getFacultyEnrollmentsByCourse: (courseId) => 
            request(`/faculty/enrollments/course/${courseId}`, { method: 'GET' }),
        getFacultyEnrollmentsBySemester: (semesterId) => 
            request(`/faculty/enrollments/semester/${semesterId}`, { method: 'GET' }),
        getFacultyEnrollmentsByStudent: (studentId) => 
            request(`/faculty/enrollments/student/${studentId}`, { method: 'GET' }),
        updateGrade: (enrollmentId, grade) => request('/faculty/grades', {
            method: 'PUT',
            body: JSON.stringify({ enrollmentId, grade })
        }),

        updateGradesBulk: (requests) => request('/faculty/grades/bulk', {
            method: 'PUT',
            body: JSON.stringify(requests)
        }),

        // Department APIs
        getDepartments: () => request('/departments', { method: 'GET' }),
        createDepartment: (name, code) =>
            request('/departments', {
                method: 'POST',
                body: JSON.stringify({ name, code })
            }),
        updateDepartment: (id, name, code) =>
            request(`/departments/${id}`, {
                method: 'PUT',
                body: JSON.stringify({ name, code })
            }),
        deleteDepartment: (id) => request(`/departments/${id}`, { method: 'DELETE' }),

        // Analytics
        getSGPATrends: (studentId) => 
            request(`/analytics/sgpa-trends/${studentId}`, { method: 'GET' }),
        getRankings: (limit = 10) => 
            request(`/analytics/rankings?limit=${limit}`, { method: 'GET' }),
        getToppers: (count = 5) => 
            request(`/analytics/toppers?count=${count}`, { method: 'GET' }),
        getCourseAnalytics: (courseId) => 
            request(`/analytics/course/${courseId}`, { method: 'GET' }),
        getClassStatistics: () => 
            request(`/analytics/class`, { method: 'GET' })
    };
})();
