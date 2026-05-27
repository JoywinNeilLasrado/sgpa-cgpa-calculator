/**
 * GradePoint UI Module
 * DOM manipulation and UI helpers
 */
import { Toast } from './components/toast.js';

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

    // Show error toast using central Toast component
    function showError(message) {
        Toast.error(message);
    }

    // Show success toast using central Toast component
    function showSuccess(message) {
        Toast.success(message);
    }

    // Show toast notification delegating to Toast component
    function showToast(message, type = 'success') {
        Toast.show(message, type);
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

    // Modern shimmering skeleton screen loader
    function showSkeletonLoader(containerId, type = 'grid', count = 3) {
        const el = document.getElementById(containerId);
        if (!el) return;

        let html = '';
        if (type === 'table') {
            html = `
                <div class="skeleton-loader">
                    <table class="table" style="width: 100%;">
                        <thead>
                            <tr>
                                <th><div class="skeleton-line short"></div></th>
                                <th><div class="skeleton-line medium"></div></th>
                                <th><div class="skeleton-line short"></div></th>
                                <th><div class="skeleton-line long"></div></th>
                            </tr>
                        </thead>
                        <tbody>
            `;
            for (let i = 0; i < count; i++) {
                html += `
                    <tr class="skeleton-row">
                        <td><div class="skeleton-line short" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line medium" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line short" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line long" style="margin: 0.5rem 0;"></div></td>
                    </tr>
                `;
            }
            html += `
                        </tbody>
                    </table>
                </div>
            `;
        } else if (type === 'profile') {
            html = `
                <div class="skeleton-loader">
                    <div class="skeleton-card" style="display: flex; flex-direction: row; gap: 1.5rem; align-items: center;">
                        <div class="skeleton-circle" style="width: 80px; height: 80px; flex-shrink: 0;"></div>
                        <div style="flex: 1; display: flex; flex-direction: column; gap: 0.75rem;">
                            <div class="skeleton-line title"></div>
                            <div class="skeleton-line medium"></div>
                            <div class="skeleton-line short"></div>
                        </div>
                    </div>
                </div>
            `;
        } else if (type === 'list') {
            html = `
                <div class="skeleton-loader">
            `;
            for (let i = 0; i < count; i++) {
                html += `
                    <div class="skeleton-card" style="padding: 1.25rem; margin-bottom: 0.75rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; gap: 1rem;">
                            <div class="skeleton-line medium" style="margin: 0;"></div>
                            <div class="skeleton-line short" style="margin: 0;"></div>
                        </div>
                    </div>
                `;
            }
            html += `</div>`;
        } else { // 'grid' or 'card'
            html = `
                <div class="skeleton-loader" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem;">
            `;
            for (let i = 0; i < count; i++) {
                html += `
                    <div class="skeleton-card">
                        <div class="skeleton-line title"></div>
                        <div class="skeleton-line long"></div>
                        <div class="skeleton-line medium"></div>
                        <div class="skeleton-line short"></div>
                    </div>
                `;
            }
            html += `</div>`;
        }
        el.innerHTML = html;
    }

    // Loading spinner backward compatibility wrapper
    function showLoading(elementId) {
        showSkeletonLoader(elementId, 'list', 3);
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

    // Debounce method for limiting search keypress events
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
        showSkeletonLoader,
        showLoading,
        hideLoading,
        formatDate,
        debounce
    };
})();

export { UI };