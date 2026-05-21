/**
 * GradePoint Transcript Page
 * Academic transcript display and PDF export
 */

(function() {
    'use strict';

    let allStudents = [];

    // Initialize
    function init() {
        console.log('Loading transcript...');
        
        // Set document date
        const today = new Date();
        document.getElementById('current-date').textContent = today.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        document.getElementById('doc-id').textContent = 'GP-' + Math.random().toString(36).substr(2, 8).toUpperCase();

        loadStudents();
    }

    // Load students
    async function loadStudents() {
        try {
            allStudents = await API.getStudents();
            const select = document.getElementById('student-select');
            
            allStudents.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.name + ' (' + s.studentId + ')';
                select.appendChild(opt);
            });

            // Add listener
            select.addEventListener('change', handleStudentChange);
            
        } catch (err) {
            console.error('Error:', err);
            UI.showError('Failed to load students');
        }
    }

    // Handle student selection
    async function handleStudentChange(e) {
        const studentId = e.target.value;
        if (!studentId) return;

        // Find student info
        const student = allStudents.find(s => s.id == studentId);
        document.getElementById('student-name').textContent = student ? student.name : '-';
        document.getElementById('student-roll').textContent = student ? student.studentId : '-';

        try {
            const data = await API.getDashboard(studentId);
            
            document.getElementById('transcript').style.display = 'block';
            renderResults(data.semesterResults);

            // Update summary
            document.getElementById('final-cgpa').textContent = (data.cgpa || 0).toFixed(2);
            document.getElementById('final-credits').textContent = data.totalCredits || 0;
            document.getElementById('final-courses').textContent = data.totalCourses || 0;
            document.getElementById('classification').textContent = getClassification(data.cgpa || 0);
            
        } catch (err) {
            console.error('Error:', err);
            UI.showError('Failed to load transcript');
        }
    }

    // Render semester results
    function renderResults(semesterResults) {
        const container = document.getElementById('results-container');
        container.innerHTML = '';

        if (!semesterResults || semesterResults.length === 0) {
            container.innerHTML = '<p style="text-align:center;color:var(--slate-light)">No results found</p>';
            return;
        }

        semesterResults.forEach(semester => {
            let rows = '';
            
            semester.courses.forEach(c => {
                const gradeClass = c.grade.replace('+', '\\+');
                rows += 
                    '<tr>' +
                        '<td class="code">' + c.courseCode + '</td>' +
                        '<td>' + c.courseName + '</td>' +
                        '<td class="credits">' + c.credits + '</td>' +
                        '<td class="points">' + c.creditPoints + '</td>' +
                        '<td><span class="grade-badge ' + gradeClass + '">' + c.grade + '</span></td>' +
                    '</tr>';
            });

            container.innerHTML += 
                '<div class="semester-group">' +
                    '<div class="semester-header">' +
                        '<div class="semester-title">Semester ' + semester.semesterNumber + '</div>' +
                        '<div class="semester-gpa">SGPA: ' + semester.sgpa.toFixed(2) + '</div>' +
                    '</div>' +
                    '<table class="results-table">' +
                        '<thead><tr><th>Code</th><th>Course</th><th>Credits</th><th>Points</th><th>Grade</th></tr></thead>' +
                        '<tbody>' + rows + '</tbody>' +
                    '</table>' +
                '</div>';
        });
    }

    // Get CGPA classification
    function getClassification(cgpa) {
        if (cgpa >= 9) return 'First Class with Distinction';
        if (cgpa >= 8) return 'First Class';
        if (cgpa >= 7) return 'Second Class (Upper)';
        if (cgpa >= 6) return 'Second Class (Lower)';
        if (cgpa >= 5) return 'Third Class';
        return 'Pass';
    }

    // Initialize on load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();