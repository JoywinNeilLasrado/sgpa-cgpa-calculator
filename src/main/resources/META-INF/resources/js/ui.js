/**
 * GradePoint UI Module
 * DOM manipulation and UI helpers
 */

const UI = (function() {
    'use strict';

    // 🛡️ DOMPurify auto-loader for advanced XSS input sanitization
    (function initDOMPurify() {
        if (typeof window !== 'undefined' && typeof window.DOMPurify === 'undefined') {
            const script = document.createElement('script');
            script.src = 'https://cdnjs.cloudflare.com/ajax/libs/dompurify/3.0.8/purify.min.js';
            script.crossOrigin = 'anonymous';
            script.referrerPolicy = 'no-referrer';
            document.head.appendChild(script);
        }
    })();

    // ⌨️ Keyboard accessibility & focus outline helpers
    (function initAccessibility() {
        if (typeof window === 'undefined') return;

        // Escape key modal closer
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal.active, [id*="modal"].active, [id*="Modal"].active').forEach(modal => {
                    modal.classList.remove('active');
                });
            }
        });

        // Outline styles for accessible tabbing (focus-visible)
        const style = document.createElement('style');
        style.id = 'accessibility-focus-style';
        style.textContent = `
            /* Focus visible accessibility states */
            button:focus-visible, input:focus-visible, select:focus-visible, a:focus-visible {
                outline: 3px solid #8b1538 !important;
                outline-offset: 2px !important;
                box-shadow: 0 0 0 4px rgba(139, 21, 56, 0.25) !important;
            }
        `;
        document.head.appendChild(style);
    })();

    // Show error toast
    function showError(message) {
        showToast(message, 'error');
    }

    // Show success toast
    function showSuccess(message) {
        showToast(message, 'success');
    }

    // Show toast notification
    function showToast(message, type) {
        const existing = document.querySelector('.toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => toast.classList.add('show'), 10);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    // Populate dropdown
    function populateSelect(selectId, items, valueField, textField) {
        const select = document.getElementById(selectId);
        if (!select) return;

        const currentValue = select.value;
        select.innerHTML = '';

        items.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item[valueField];
            opt.textContent = item[textField];
            select.appendChild(opt);
        });

        if (currentValue) select.value = currentValue;
    }

    // Create table row
    function createTableRow(cells, actions) {
        const tr = document.createElement('tr');

        cells.forEach(text => {
            const td = document.createElement('td');
            td.textContent = text;
            tr.appendChild(td);
        });

        if (actions) {
            const td = document.createElement('td');
            td.innerHTML = actions;
            tr.appendChild(td);
        }

        return tr;
    }

    // Filter table rows
    function filterTable(tableId, filterInputId) {
        const filter = document.getElementById(filterInputId)?.value.toLowerCase() || '';
        const rows = document.querySelectorAll(`#${tableId} tbody tr`);

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(filter) ? '' : 'none';
        });
    }

    // Show/hide section
    function toggleSection(sectionId, show) {
        const section = document.getElementById(sectionId);
        if (section) {
            section.style.display = show ? '' : 'none';
        }
    }

    // Set active tab
    function setActiveTab(tabButtons, activeButton) {
        tabButtons.forEach(btn => btn.classList.remove('active'));
        activeButton.classList.add('active');
    }

    // Loading spinner
    function showLoading(elementId) {
        const el = document.getElementById(elementId);
        if (el) el.innerHTML = '<div class="spinner"></div>';
    }

    function hideLoading(elementId, content) {
        const el = document.getElementById(elementId);
        if (el) el.innerHTML = content || '';
    }

    // Format date
    function formatDate(dateStr) {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    function escapeHTML(str) {
        if (str == null) return '';
        if (typeof window !== 'undefined' && window.DOMPurify) {
            return window.DOMPurify.sanitize(String(str));
        }
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }


    // Public API
    return {
        escapeHTML,
        showError,
        showSuccess,
        showToast,
        populateSelect,
        createTableRow,
        filterTable,
        toggleSection,
        setActiveTab,
        showLoading,
        hideLoading,
        formatDate,
        debounce
    };
})();

export { UI };