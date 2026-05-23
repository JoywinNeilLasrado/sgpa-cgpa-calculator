// UI Renderer utilities for GradePoint frontend
// Provides functions to render common UI components

import { UI } from "../ui.js";

/**
 * Render a list of students into the given table body element.
 * @param {HTMLElement} tbody - The <tbody> element where rows will be inserted.
 * @param {Array} students - Array of student objects with properties id, name, studentId, branch.
 */
export function renderStudentList(tbody, students) {
  // Clear existing rows
  tbody.innerHTML = "";
  if (!Array.isArray(students) || students.length === 0) {
    const emptyRow = document.createElement("tr");
    const td = document.createElement("td");
    td.colSpan = 6;
    td.textContent = "No student records found.";
    emptyRow.appendChild(td);
    tbody.appendChild(emptyRow);
    return;
  }
  students.forEach((s) => {
    const actions = `
      <button class="btn btn-secondary btn-sm" style="margin-right: 0.5rem;" onclick="editStudent(${s.id}, '${s.name}', '${s.studentId}', '${s.branch}', '${s.dateOfBirth || ''}')">Edit</button>
      <button class="btn btn-danger btn-sm" style="margin-right: 0.5rem;" onclick="deleteStudent(${s.id})">Delete</button>
      <button class="btn btn-warning btn-sm" onclick="openPasswordModal('${s.username}')">Change Password</button>
    `;
    const row = UI.createTableRow([
      s.id,
      s.name,
      s.studentId,
      s.branch,
      '••••',
    ], actions);
    tbody.appendChild(row);
  });
}

export default { renderStudentList };
