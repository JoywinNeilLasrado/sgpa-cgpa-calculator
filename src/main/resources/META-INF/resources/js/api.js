/**
 * GradePoint API Module
 * Handles all REST API calls to the backend
 */

const API = (function() {
    'use strict';

    const BASE_URL = '/api';

    // Students
    async function getStudents() {
        const res = await fetch(`${BASE_URL}/students`);
        return res.json();
    }

    async function getStudent(id) {
        const res = await fetch(`${BASE_URL}/students/${id}`);
        return res.json();
    }

    async function createStudent(data) {
        const res = await fetch(`${BASE_URL}/students`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    }

    async function deleteStudent(id) {
        await fetch(`${BASE_URL}/students/${id}`, { method: 'DELETE' });
    }

    // Semesters
    async function getSemesters() {
        const res = await fetch(`${BASE_URL}/semesters`);
        return res.json();
    }

    async function createSemester(data) {
        const res = await fetch(`${BASE_URL}/semesters`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    }

    async function deleteSemester(id) {
        await fetch(`${BASE_URL}/semesters/${id}`, { method: 'DELETE' });
    }

    // Courses
    async function getCourses() {
        const res = await fetch(`${BASE_URL}/courses`);
        return res.json();
    }

    async function createCourse(data) {
        const res = await fetch(`${BASE_URL}/courses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    }

    async function deleteCourse(id) {
        await fetch(`${BASE_URL}/courses/${id}`, { method: 'DELETE' });
    }

    // Enrollments
    async function getEnrollments() {
        const res = await fetch(`${BASE_URL}/enrollments`);
        return res.json();
    }

    async function createEnrollment(data) {
        const res = await fetch(`${BASE_URL}/enrollments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    }

    async function deleteEnrollment(id) {
        await fetch(`${BASE_URL}/enrollments/${id}`, { method: 'DELETE' });
    }

    // Grade Calculations
    async function calculateSGPA(studentId, semesterId) {
        const res = await fetch(`${BASE_URL}/sgpa/student/${studentId}/semester/${semesterId}`);
        return res.json();
    }

    async function calculateCGPA(studentId) {
        const res = await fetch(`${BASE_URL}/cgpa/student/${studentId}`);
        return res.json();
    }

    // Dashboard
    async function getDashboard(studentId) {
        const res = await fetch(`${BASE_URL}/students/${studentId}/dashboard`);
        return res.json();
    }

    // Grade Scale
    async function getGradeScale() {
        const res = await fetch(`${BASE_URL}/grades/scale`);
        return res.json();
    }

    async function getGradeFromMarks(marks) {
        const res = await fetch(`${BASE_URL}/grades/from-marks/${marks}`);
        return res.json();
    }

    // Public API
    return {
        getStudents,
        getStudent,
        createStudent,
        deleteStudent,
        getSemesters,
        createSemester,
        deleteSemester,
        getCourses,
        createCourse,
        deleteCourse,
        getEnrollments,
        createEnrollment,
        deleteEnrollment,
        calculateSGPA,
        calculateCGPA,
        getDashboard,
        getGradeScale,
        getGradeFromMarks
    };
})();