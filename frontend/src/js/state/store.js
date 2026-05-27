// js/state/store.js
export const Store = {
    state: {
        students: [],
        courses: [],
        semesters: [],
        enrollments: [],
        departments: [],
        faculty: []
    },
    
    set: (key, value) => {
        Store.state[key] = value;
    },
    
    get: (key) => {
        return Store.state[key] || [];
    }
};

// Global backward compatibility bridge
window.Store = Store;
