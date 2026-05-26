// js/services/errorHandler.js
import { Toast } from '../components/toast.js';
import { Auth } from '../auth/Auth.js';

export const handleAPIError = (err, context = 'perform action') => {
    console.error(`API Error during [${context}]:`, err);
    if (err && (err.status === 401 || err.statusCode === 401)) {
        Toast.error('Session expired. Logging out...');
        setTimeout(() => {
            Auth.logout();
        }, 1500);
        return;
    }
    const message = err.message || err.responseText || 'Request failed';
    Toast.error(`Failed to ${context}: ${message}`);
};

// Global backward compatibility bridge
window.handleAPIError = handleAPIError;
export default handleAPIError;
