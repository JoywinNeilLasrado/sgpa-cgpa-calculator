// js/components/toast.js
import { Config } from '../config.js';

export const Toast = {
    show: (message, type = 'success') => {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type} show`;
        toast.innerHTML = `
            <span>${type === 'success' ? '' : type === 'error' ? '' : ''}</span>
            <span>${message}</span>
        `;
        container.appendChild(toast);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 400);
        }, Config.TOAST_DURATION);
    },
    success: (msg) => Toast.show(msg, 'success'),
    error: (msg) => Toast.show(msg, 'error'),
    info: (msg) => Toast.show(msg, 'info')
};

// Global backward compatibility bridge
window.Toast = Toast;
window.showToast = Toast.show;
