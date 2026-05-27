// js/components/modal.js
export const Modal = {
    open: (modalId, username = '') => {
        const modal = document.getElementById(modalId);
        if (modal) {
            if (username) {
                modal.dataset.username = username;
            }
            modal.classList.add('active');
        }
    },
    close: (modalId) => {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
            const newPasswordInput = document.getElementById('newPasswordInput');
            if (newPasswordInput) newPasswordInput.value = '';
        }
    }
};

// Global backward compatibility bridge
window.Modal = Modal;
