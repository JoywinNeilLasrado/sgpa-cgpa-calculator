// Centralized API Service Module
// Complete ES6 module API communication layer with JWT authentication.
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

const interceptors = {
    request: [],
    response: []
};

let activeRequests = 0;

function showGlobalSpinner() {
    activeRequests++;
    let overlay = document.getElementById('global-ajax-loader');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'global-ajax-loader';
        overlay.className = 'global-ajax-loader';
        overlay.innerHTML = `
            <div class="loader-spinner"></div>
            <div class="loader-text">Processing request...</div>
        `;
        document.body.appendChild(overlay);

        if (!document.getElementById('global-loader-style')) {
            const style = document.createElement('style');
            style.id = 'global-loader-style';
            style.textContent = `
                .global-ajax-loader {
                    position: fixed;
                    top: 0; left: 0; right: 0; bottom: 0;
                    background: rgba(15, 23, 42, 0.55);
                    backdrop-filter: blur(3px);
                    display: flex; flex-direction: column;
                    align-items: center; justify-content: center;
                    z-index: 999999;
                    opacity: 0; pointer-events: all;
                    transition: opacity 0.2s ease;
                }
                .global-ajax-loader.show {
                    opacity: 1;
                }
                .loader-spinner {
                    width: 48px; height: 48px;
                    border: 4.5px solid rgba(255, 255, 255, 0.15);
                    border-left-color: #8b1538;
                    border-radius: 50%;
                    animation: global-spin 0.75s linear infinite;
                }
                .loader-text {
                    margin-top: 16px;
                    color: #f8fafc;
                    font-weight: 600;
                    font-size: 0.9rem;
                    letter-spacing: 0.05em;
                }
                @keyframes global-spin {
                    to { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(style);
        }
    }
    setTimeout(() => overlay.classList.add('show'), 5);
}

function hideGlobalSpinner() {
    activeRequests = Math.max(0, activeRequests - 1);
    if (activeRequests === 0) {
        const overlay = document.getElementById('global-ajax-loader');
        if (overlay) {
            overlay.classList.remove('show');
            setTimeout(() => {
                if (activeRequests === 0 && overlay.parentNode) {
                    overlay.remove();
                }
            }, 200);
        }
    }
}

export class APIError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = 'APIError';
        this.status = status;
        this.data = data;
    }
}

async function request(endpoint, options = {}) {
    // 1. Run request interceptors
    for (const interceptor of interceptors.request) {
        options = (await interceptor(endpoint, options)) || options;
    }

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

    // 2. Trigger loading spinner overlay
    showGlobalSpinner();

    try {
        const res = await fetch(url, config);
        
        // 3. Hide loading spinner overlay
        hideGlobalSpinner();

        // 4. Run response interceptors
        for (const interceptor of interceptors.response) {
            await interceptor(res);
        }

        if (res.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
            return;
        }
        if (!res.ok) {
            let errorMsg = `API Error: ${res.statusText}`;
            let errData = {};
            try {
                errData = JSON.parse(await res.text());
                if (errData && errData.message) {
                    errorMsg = errData.message;
                }
            } catch (e) {
                // Fallback to standard error msg
            }
            throw new APIError(errorMsg, res.status, errData);
        }
        const text = await res.text();
        return text ? JSON.parse(text) : {};
    } catch (error) {
        hideGlobalSpinner();
        throw error;
    }
}

export const apiService = {
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
    createCourse: (courseCode, courseName, credits, semesterId, facultyId, courseType = 'THEORY') => request('/courses', {
        method: 'POST',
        body: JSON.stringify({ courseCode, courseName, credits, semesterId, facultyId, courseType })
    }),
    updateCourse: (id, courseCode, courseName, credits, semesterId, facultyId, courseType = 'THEORY') => request(`/courses/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ courseCode, courseName, credits, semesterId, facultyId, courseType })
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
    updateEnrollment: (id, payload) => request(`/enrollments/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
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

    getFacultyCourses: () => request('/faculty/courses', { method: 'GET' }),

    getFacultyEnrollmentsByCourse: (courseId) => 
        request(`/faculty/enrollments/course/${courseId}`, { method: 'GET' }),
    getFacultyEnrollmentsBySemester: (semesterId) => 
        request(`/faculty/enrollments/semester/${semesterId}`, { method: 'GET' }),
    getFacultyEnrollmentsByStudent: (studentId) => 
        request(`/faculty/enrollments/student/${studentId}`, { method: 'GET' }),
    updateGrade: (payload) => request('/faculty/grades', {
        method: 'PUT',
        body: JSON.stringify(payload)
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
        request(`/analytics/class`, { method: 'GET' }),

    // Interceptors
    addRequestInterceptor: (fn) => interceptors.request.push(fn),
    addResponseInterceptor: (fn) => interceptors.response.push(fn)
};

// Expose globally for backward compatibility with inline scripts
if (typeof window !== 'undefined') {
    window.API = apiService;
}
