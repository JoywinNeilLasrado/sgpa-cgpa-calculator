/**
 * GradePoint Shared Navigation Bar Component
 */
(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', () => {
        renderNavbar();
    });

    function escapeHTML(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function renderNavbar() {
        const placeholder = document.getElementById('nav-placeholder');
        if (!placeholder) {
            console.warn('GradePoint Nav Warning: No element with id "nav-placeholder" found on page.');
            return;
        }

        const token = localStorage.getItem('token');
        const userJson = localStorage.getItem('user');
        const user = userJson ? JSON.parse(userJson) : null;
        
        const path = window.location.pathname;
        const pageName = path.substring(path.lastIndexOf('/') + 1) || 'home.html';

        let brandHref = '/home.html';
        let navLinksHtml = '';

        if (token && user) {
            const role = user.role || '';
            const username = user.username || 'User';

            if (role === 'ADMIN') {
                brandHref = '/admin.html';
                navLinksHtml = `
                    <a href="/admin.html" class="nav-link ${pageName === 'admin.html' ? 'active' : ''}">Admin Portal</a>
                    <a href="/analytics.html" class="nav-link ${pageName === 'analytics.html' ? 'active' : ''}">Analytics</a>
                    <a href="/transcript.html" class="nav-link ${pageName === 'transcript.html' ? 'active' : ''}">Transcript</a>
                    <a href="/profile.html" class="nav-link ${pageName === 'profile.html' ? 'active' : ''}">Profile</a>
                `;
            } else if (role === 'FACULTY') {
                brandHref = '/faculty-grades.html';
                navLinksHtml = `
                    <a href="/faculty-grades.html" class="nav-link ${pageName === 'faculty-grades.html' ? 'active' : ''}">Grade Entry</a>
                    <a href="/analytics.html" class="nav-link ${pageName === 'analytics.html' ? 'active' : ''}">System Rankings</a>
                    <a href="/transcript.html" class="nav-link ${pageName === 'transcript.html' ? 'active' : ''}">Transcripts</a>
                    <a href="/profile.html" class="nav-link ${pageName === 'profile.html' ? 'active' : ''}">Profile</a>
                `;
            } else {
                // STUDENT
                brandHref = '/student.html';
                navLinksHtml = `
                    <a href="/student.html" class="nav-link ${pageName === 'student.html' ? 'active' : ''}">Dashboard</a>
                    <a href="/analytics.html" class="nav-link ${pageName === 'analytics.html' ? 'active' : ''}">Analytics</a>
                    <a href="/transcript.html" class="nav-link ${pageName === 'transcript.html' ? 'active' : ''}">Transcript</a>
                    <a href="/profile.html" class="nav-link ${pageName === 'profile.html' ? 'active' : ''}">Profile</a>
                `;
            }

            const badgeBg = role === 'ADMIN' ? 'var(--crimson)' : role === 'FACULTY' ? 'var(--gold-dark)' : 'var(--slate)';
            navLinksHtml += `
                <div class="nav-user-info">
                    <div class="user-pill">
                        <span id="nav-username">${escapeHTML(username)}</span>
                        <span class="user-badge" style="background: ${badgeBg}">${role}</span>
                    </div>
                    <button class="btn btn-primary btn-sm" id="nav-logout-btn">Logout</button>
                </div>
            `;
        } else {
            // Anonymous / landing
            brandHref = '/home.html';
            navLinksHtml = `
                <a href="/home.html" class="nav-link ${pageName === 'home.html' ? 'active' : ''}">Home</a>
                <a href="/analytics.html" class="nav-link ${pageName === 'analytics.html' ? 'active' : ''}">Analytics</a>
                <a href="/transcript.html" class="nav-link ${pageName === 'transcript.html' ? 'active' : ''}">Transcript</a>
                <a href="/student-dashboard.html" class="nav-link ${pageName === 'student-dashboard.html' ? 'active' : ''}">My Dashboard</a>
                <a href="/profile.html" class="nav-link ${pageName === 'profile.html' ? 'active' : ''}">Profile</a>
                <a href="/login.html" class="nav-link ${pageName === 'login.html' ? 'active' : ''}" id="login-link">Login</a>
            `;
        }

        const navHtml = `
            <div class="container nav-inner">
                <a href="${brandHref}" class="nav-logo">
                    <div class="nav-logo-icon">🎓</div>
                    <div class="nav-logo-text">
                        <h1>GradePoint</h1>
                        <span>Academic Excellence</span>
                    </div>
                </a>
                <div class="nav-links">
                    ${navLinksHtml}
                </div>
            </div>
        `;

        placeholder.innerHTML = navHtml;

        // Bind logout action if logout button exists
        const logoutBtn = document.getElementById('nav-logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                localStorage.clear();
                window.location.href = '/';
            });
        }
    }
})();
