/**
 * GradePoint Analytics Page
 * Performance visualization and stats
 */
import { handleAPIError } from './services/errorHandler.js';
import { Toast } from './components/toast.js';

(function() {
    'use strict';

    let dashboardData = null;

    // Initialize
    async function init() {
        console.log('Loading analytics...');
        
        try {
            const students = await API.getStudents();
            const select = document.getElementById('student-select');
            
            students.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.name + ' (' + s.studentId + ')';
                select.appendChild(opt);
            });

            // Add listener
            select.addEventListener('change', handleStudentChange);
            
        } catch (err) {
            handleAPIError(err, 'load students');
            Toast.error('Failed to load students');
        }
    }

    // Handle student selection
    async function handleStudentChange(e) {
        const studentId = e.target.value;
        if (!studentId) return;

        try {
            dashboardData = await API.getDashboard(studentId);
            showStats();
            renderGPAChart(dashboardData.sgpaBySemester);
            renderGradeDist(dashboardData.gradeDistribution);
            updateQuickStats();
            
        } catch (err) {
            handleAPIError(err, 'load dashboard');
            Toast.error('Failed to load dashboard');
        }
    }

    // Show stats sections
    function showStats() {
        document.getElementById('stats-grid').style.display = 'grid';
        document.getElementById('charts-grid').style.display = 'grid';
        document.getElementById('quick-stats').style.display = 'grid';

        // Animate CGPA value
        animateValue('cgpa-display', dashboardData.cgpa || 0);
        
        document.getElementById('total-credits').textContent = dashboardData.totalCredits || 0;
        document.getElementById('total-courses').textContent = dashboardData.totalCourses || 0;
        document.getElementById('semesters-count').textContent = dashboardData.semestersCompleted || 0;
    }

    // Update quick stats
    function updateQuickStats() {
        const sgpas = Object.values(dashboardData.sgpaBySemester || {});
        const highest = Math.max(...sgpas, 0);
        
        document.getElementById('highest-gpa').textContent = highest.toFixed(2);
        document.getElementById('pass-rate').textContent = (dashboardData.passRate || 0) + '%';
        document.getElementById('outstanding').textContent = dashboardData.outstandingCount || 0;
        
        if (sgpas.length >= 2) {
            const trend = (sgpas[sgpas.length-1] - sgpas[sgpas.length-2]).toFixed(2);
            document.getElementById('improvement').textContent = (trend >= 0 ? '+' : '') + trend;
        }
    }

    // Animate numeric value
    function animateValue(id, endValue) {
        const elem = document.getElementById(id);
        const duration = 1000;
        const startTime = performance.now();
        
        function update(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            elem.textContent = (endValue * eased).toFixed(2);
            if (progress < 1) requestAnimationFrame(update);
        }
        requestAnimationFrame(update);
    }

    // Render GPA bar chart
    function renderGPAChart(sgpaData) {
        const container = document.getElementById('gpa-chart');
        container.innerHTML = '';
        if (!sgpaData) return;

        const colors = ['var(--crimson)', 'var(--gold)', '#6366f1', '#22d3ee'];
        
        Object.entries(sgpaData).forEach(([sem, gpa], i) => {
            const pct = (gpa / 10) * 100;
            container.innerHTML += 
                '<div class="bar-item">' +
                    '<div class="bar-label">' + sem + '</div>' +
                    '<div class="bar-track">' +
                        '<div class="bar-fill" style="width:' + pct + '%;background:linear-gradient(90deg,' + colors[i % 4] + ',' + colors[(i + 1) % 4] + ')"></div>' +
                    '</div>' +
                    '<div class="bar-value">' + gpa.toFixed(2) + '</div>' +
                '</div>';
        });
    }

    // Render grade distribution
    function renderGradeDist(grades) {
        const container = document.getElementById('grade-dist');
        container.innerHTML = '';
        
        const displayGrades = ['O', 'A+', 'A', 'B+', 'B', 'C'];
        
        displayGrades.forEach(g => {
            container.innerHTML += 
                '<div class="grade-item">' +
                    '<div class="grade-letter">' + g + '</div>' +
                    '<div class="grade-count">' + (grades[g] || 0) + '</div>' +
                '</div>';
        });
    }

    // Initialize on load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();