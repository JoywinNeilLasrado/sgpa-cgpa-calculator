/**
 * core.js
 * Global application backbone. 
 * Handles layout injection, authentication state, and global utilities (Toasts, Logout).
 */

(function initCore() {
    // 1. Inject Global Head Metadata & Styles if not already present
    if (!document.querySelector('link[href="/css/theme.css"]')) {
        const fonts = document.createElement('link');
        fonts.rel = 'stylesheet';
        fonts.href = 'https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,600;0,700;1,400&family=DM+Sans:wght@400;500;600;700&display=swap';
        document.head.appendChild(fonts);

        const theme = document.createElement('link');
        theme.rel = 'stylesheet';
        theme.href = '/css/theme.css';
        document.head.appendChild(theme);
    }

    // 2. Inject Global Toast Container
    if (!document.getElementById('toast-container')) {
        const toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    // 3. Expose Global Utilities
    window.showToast = function(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return; // Failsafe
        
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
    };

    window.logout = function() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
    };

    window.checkAuth = function(requiredRole = null) {
        const token = localStorage.getItem('token');
        const userJson = localStorage.getItem('user');
        
        const isLoginPage = window.location.pathname === '/' || window.location.pathname === '/index.html' || window.location.pathname === '/login.html';

        if (!token || !userJson) {
            if (!isLoginPage) {
                window.location.href = '/';
            }
            return null;
        }

        try {
            const user = JSON.parse(userJson);
            
            // If on login page and already authenticated, redirect to dashboard
            if (isLoginPage) {
                if (user.role === 'ADMIN') window.location.href = '/admin.html';
                else if (user.role === 'FACULTY') window.location.href = '/faculty-grades.html';
                else window.location.href = '/student.html';
                return user;
            }
            
            if (requiredRole && user.role !== requiredRole) {
                // If they don't have the required role, redirect them to their respective dashboards
                if (user.role === 'ADMIN') window.location.href = '/admin.html';
                else if (user.role === 'FACULTY') window.location.href = '/faculty-grades.html';
                else window.location.href = '/student.html';
                return null;
            }
            return user;
        } catch (e) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            if (!isLoginPage) {
                window.location.href = '/';
            }
            return null;
        }
    };
    // 4. Automatically check authentication on page load
    const path = window.location.pathname;
    let requiredRole = null;
    
    // Deduce required role from URL (simple convention for this app)
    if (path.includes('admin')) {
        requiredRole = 'ADMIN';
    } else if (path.includes('faculty')) {
        requiredRole = 'FACULTY';
    } else if (path.includes('student') || path.includes('student-dashboard')) {
        requiredRole = 'STUDENT';
    }
    
    // Only 'home.html' is fully public without redirects for logged in users
    if (path !== '/home.html') {
        window.checkAuth(requiredRole);
    }
})();
