// js/auth/Auth.js
export const Auth = {
    isAuthenticated: () => !!localStorage.getItem('token'),
    
    getToken: () => localStorage.getItem('token'),
    
    getUser: () => {
        const userJson = localStorage.getItem('user');
        try {
            return userJson ? JSON.parse(userJson) : null;
        } catch (e) {
            console.error('Failed to parse cached user:', e);
            return null;
        }
    },
    
    login: (token, user) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
    },
    
    logout: () => {
        localStorage.clear();
        window.location.href = '/';
    },
    
    requireRole: (requiredRole = null) => {
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
            
            if (isLoginPage) {
                if (user.role === 'ADMIN') window.location.href = '/admin.html';
                else if (user.role === 'FACULTY') window.location.href = '/faculty-grades.html';
                else window.location.href = '/student.html';
                return user;
            }
            
            if (requiredRole && user.role !== requiredRole) {
                Auth.logout();
                return null;
            }
            return user;
        } catch (err) {
            console.error('Failed to parse credentials:', err);
            Auth.logout();
            return null;
        }
    }
};

// Global backward compatibility bridge
window.Auth = Auth;
window.logout = Auth.logout;
window.checkAuth = Auth.requireRole;
