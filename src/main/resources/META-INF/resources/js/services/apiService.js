// Centralized API Service Wrapper
// This module re-exports the existing global API for easier imports in ES6 modules.
export const apiService = typeof API !== 'undefined' ? API : (() => {
  console.error('Global API object not found.');
  return {};
})();
