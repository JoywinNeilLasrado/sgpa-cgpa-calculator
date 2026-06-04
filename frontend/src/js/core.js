/**
 * core.js
 * Global application backbone. 
 * Handles layout injection, authentication state, and global utilities (Toasts, Logout).
 */

(function initCore() {
    // 0. Premium Graceful Page Transition Loader
    const observer = new MutationObserver((mutations, obs) => {
        if (document.body) {
            injectPageLoader();
            obs.disconnect(); // Stop observing once injected
        }
    });
    observer.observe(document.documentElement, { childList: true, subtree: true });

    function injectPageLoader() {
        if (document.getElementById('gp-page-loader')) return; // Avoid duplicate
        
        // Inject Stylesheet in <head>
        const style = document.createElement('style');
        style.id = 'gp-loader-styles';
        style.textContent = `
            .page-loader {
                position: fixed;
                inset: 0;
                background: #faf9f7; /* Matches var(--paper) */
                z-index: 999999;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-direction: column;
                gap: 1.5rem;
                transition: opacity 0.4s cubic-bezier(0.16, 1, 0.3, 1), visibility 0.4s cubic-bezier(0.16, 1, 0.3, 1);
                opacity: 1;
                visibility: visible;
            }
            .page-loader::before {
                content: '';
                position: absolute;
                inset: 0;
                background: 
                    radial-gradient(circle at 10% 20%, rgba(139, 21, 56, 0.04) 0%, transparent 45%),
                    radial-gradient(circle at 90% 80%, rgba(201, 162, 39, 0.05) 0%, transparent 45%);
                pointer-events: none;
            }
            .page-loader.fade-out {
                opacity: 0;
                visibility: hidden;
            }
            .page-loader-spinner {
                width: 64px;
                height: 64px;
                border: 3.5px solid rgba(139, 21, 56, 0.08);
                border-top: 3.5px solid #8b1538; /* var(--crimson) */
                border-radius: 50%;
                animation: loader-spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
                position: relative;
            }
            .page-loader-spinner::after {
                content: '';
                position: absolute;
                inset: 6px;
                border: 3.5px solid rgba(201, 162, 39, 0.08);
                border-bottom: 3.5px solid #c9a227; /* var(--gold) */
                border-radius: 50%;
                animation: loader-spin-reverse 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
            }
            .page-loader-brand {
                font-family: 'Cormorant Garamond', Georgia, serif;
                font-size: 2.5rem;
                font-weight: 700;
                color: #8b1538;
                letter-spacing: -0.015em;
                animation: loader-pulse 2s ease-in-out infinite;
                z-index: 10;
                text-shadow: 0 2px 12px rgba(139, 21, 56, 0.04);
            }
            .page-loader-sub {
                font-size: 0.72rem;
                font-family: 'DM Sans', sans-serif;
                text-transform: uppercase;
                letter-spacing: 0.22em;
                color: #64748b;
                font-weight: 700;
                margin-top: -0.8rem;
                z-index: 10;
            }
            @keyframes loader-spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
            @keyframes loader-spin-reverse {
                0% { transform: rotate(360deg); }
                100% { transform: rotate(0deg); }
            }
            @keyframes loader-pulse {
                0%, 100% { opacity: 0.7; transform: scale(0.98); }
                50% { opacity: 1; transform: scale(1.02); }
            }
        `;
        document.head.appendChild(style);

        // Create and Prepend Loader Div to Body
        const loader = document.createElement('div');
        loader.id = 'gp-page-loader';
        loader.className = 'page-loader';
        loader.innerHTML = `
            <div class="page-loader-spinner"></div>
            <div class="page-loader-brand">GradePoint</div>
            <div class="page-loader-sub">Academic Excellence Tracker</div>
        `;
        document.body.insertBefore(loader, document.body.firstChild);
    }

    window.hidePageLoader = function() {
        const loader = document.getElementById('gp-page-loader');
        if (loader && !loader.classList.contains('fade-out')) {
            loader.classList.add('fade-out');
            setTimeout(() => {
                loader.remove();
                const style = document.getElementById('gp-loader-styles');
                if (style) style.remove();
            }, 500);
        }
    };

    // Failsafe auto-dismiss after 1 second
    setTimeout(() => {
        if (window.hidePageLoader) window.hidePageLoader();
    }, 1000);

    // 1. Core JS execution (Auth and Global Utilities)
    // CSS is now loaded natively via standard <link> tags in HTML for smooth rendering.

    // 2. Inject Global Toast Container
    document.addEventListener('DOMContentLoaded', () => {
        if (!document.getElementById('toast-container')) {
            const toastContainer = document.createElement('div');
            toastContainer.id = 'toast-container';
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }
    });

    // 3. Expose Global Utilities
    window.showToast = function(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return; // Failsafe
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type} show`;
        toast.innerHTML = `
            <span>${type === 'success' ? '' : ''}</span>
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
            } else {
                // If on login page and not authenticated, dismiss loader early for speed
                setTimeout(() => {
                    if (window.hidePageLoader) window.hidePageLoader();
                }, 50);
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
            } else {
                setTimeout(() => {
                    if (window.hidePageLoader) window.hidePageLoader();
                }, 50);
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
