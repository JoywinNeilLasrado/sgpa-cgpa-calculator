import { apiService } from './services/apiService.js';

if (typeof window !== 'undefined') {
    window.API = apiService;
}
