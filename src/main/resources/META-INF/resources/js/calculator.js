/**
 * GradePoint Calculator Service
 * Logic for SGPA and CGPA calculations
 */

const Calculator = (function() {
    'use strict';

    // Grade point mappings
    const GRADE_POINTS = {
        'O': 10,
        'A+': 9,
        'A': 8,
        'B+': 7,
        'B': 6,
        'C': 5,
        'P': 4,
        'F': 0
    };

    // Convert grade to points
    function gradeToPoints(grade) {
        return GRADE_POINTS[grade] || 0;
    }

    // Calculate SGPA from course list
    function calculateSGPA(courses) {
        if (!courses || courses.length === 0) return 0;

        let totalCredits = 0;
        let totalPoints = 0;

        courses.forEach(course => {
            const credits = course.credits || 0;
            const gradePoints = gradeToPoints(course.grade);
            totalCredits += credits;
            totalPoints += credits * gradePoints;
        });

        if (totalCredits === 0) return 0;
        return roundToTwo(totalPoints / totalCredits);
    }

    // Calculate CGPA - excludes F grades
    function calculateCGPA(courses) {
        if (!courses || courses.length === 0) return 0;

        let totalCredits = 0;
        let totalPoints = 0;

        courses.forEach(course => {
            if (course.grade === 'F') return;
            const credits = course.credits || 0;
            const gradePoints = gradeToPoints(course.grade);
            totalCredits += credits;
            totalPoints += credits * gradePoints;
        });

        if (totalCredits === 0) return 0;
        return roundToTwo(totalPoints / totalCredits);
    }

    // Get grade from marks
    function gradeFromMarks(marks) {
        if (marks >= 90) return { grade: 'O', points: 10 };
        if (marks >= 80) return { grade: 'A+', points: 9 };
        if (marks >= 70) return { grade: 'A', points: 8 };
        if (marks >= 60) return { grade: 'B+', points: 7 };
        if (marks >= 55) return { grade: 'B', points: 6 };
        if (marks >= 50) return { grade: 'C', points: 5 };
        if (marks >= 40) return { grade: 'P', points: 4 };
        return { grade: 'F', points: 0 };
    }

    // Round to 2 decimals
    function roundToTwo(num) {
        return Math.round(num * 100) / 100;
    }

    // Format GPA for display
    function formatGPA(value) {
        return value.toFixed(2);
    }

    // Get classification from CGPA
    function getClassification(cgpa) {
        if (cgpa >= 9.0) return 'First Class with Distinction';
        if (cgpa >= 8.0) return 'First Class';
        if (cgpa >= 7.0) return 'Second Class (Upper)';
        if (cgpa >= 6.0) return 'Second Class (Lower)';
        if (cgpa >= 5.0) return 'Third Class';
        return 'Pass';
    }

    // Public API
    return {
        gradeToPoints,
        calculateSGPA,
        calculateCGPA,
        gradeFromMarks,
        roundToTwo,
        formatGPA,
        getClassification,
        GRADE_POINTS
    };
})();