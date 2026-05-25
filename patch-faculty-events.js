const fs = require('fs');
let code = fs.readFileSync('src/main/resources/META-INF/resources/js/events/facultyEvents.js', 'utf8');

const replacement = `        let enrollments = [];
        let headersHtml = '';
        if (type === 'course') {
          const courseId = document.getElementById('course-select').value;
          if (!courseId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsByCourse(courseId);
          loadCourseAnalytics(courseId);
        } else if (type === 'semester') {
          const semesterId = document.getElementById('semester-select').value;
          if (!semesterId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsBySemester(semesterId);
          const semesterSelect = document.getElementById('semester-select');
          renderLocalAnalytics(semesterSelect.options[semesterSelect.selectedIndex].text, 'Semester Level Summary', enrollments);
        } else if (type === 'student') {
          const studentId = document.getElementById('student-select').value;
          if (!studentId) { resetGridDisplay(); return; }
          enrollments = await API.getFacultyEnrollmentsByStudent(studentId);
          const studentSelect = document.getElementById('student-select');
          renderLocalAnalytics(studentSelect.options[studentSelect.selectedIndex].text, 'Student Level Summary', enrollments);
        }
        
        originalEnrollments = enrollments;
        const tbody = document.getElementById('spreadsheet-tbody');
        const thead = document.getElementById('spreadsheet-headers');

        if (enrollments.length === 0) {
          thead.innerHTML = '<th>No Data</th>';
          tbody.innerHTML = '<tr><td colspan="15" style="text-align: center; padding: 2rem; color: var(--slate-light);">No enrollment files found matching filter.</td></tr>';
        } else {
          // Determine dominant course type if possible (especially for course filter)
          const firstCourseType = enrollments[0].courseType || 'THEORY';
          const isMixed = type !== 'course';
          
          let hHtml = '';
          if (type === 'course') {
              hHtml += '<th><span class="modified-indicator"></span>Student Name</th><th>Student Roll</th><th>Credits</th>';
          } else if (type === 'semester') {
              hHtml += '<th><span class="modified-indicator"></span>Student Name</th><th>Course Code</th><th>Course Name</th>';
          } else {
              hHtml += '<th><span class="modified-indicator"></span>Course Code</th><th>Course Name</th><th>Semester</th>';
          }

          if (isMixed || firstCourseType === 'THEORY' || firstCourseType === 'INTEGRATED') {
              hHtml += '<th style="width: 7%;">Test 1</th><th style="width: 7%;">Test 2</th><th style="width: 7%;">Assign</th><th style="width: 7%;">OAA</th>';
          }
          if (isMixed || firstCourseType === 'LABORATORY' || firstCourseType === 'INTEGRATED') {
              hHtml += '<th style="width: 7%;">Reg Lab</th><th style="width: 7%;">Lab Test</th><th style="width: 7%;">Lab Rec</th>';
          }
          hHtml += '<th style="width: 8%;">SEE</th><th style="width: 6%;">Grace</th><th style="width: 10%;">Grade</th>';
          
          thead.innerHTML = hHtml;

          tbody.innerHTML = enrollments.map(e => {
            let col1 = '', col2 = '', col3 = '';
            if (type === 'course') {
              col1 = e.studentName || e.student?.name || 'N/A';
              col2 = e.studentRollNumber || e.student?.studentId || 'N/A';
              col3 = \`\${e.credits || e.course?.credits || 4} Credits\`;
            } else if (type === 'semester') {
              col1 = e.studentName || e.student?.name || 'N/A';
              col2 = e.courseCode || e.course?.courseCode || 'N/A';
              col3 = e.courseName || e.course?.courseName || 'N/A';
            } else if (type === 'student') {
              col1 = e.courseCode || e.course?.courseCode || 'N/A';
              col2 = e.courseName || e.course?.courseName || 'N/A';
              col3 = \`Semester \${e.semesterNumber || e.course?.semester?.semesterNumber || 'N/A'}\`;
            }
            
            const cType = e.courseType || 'THEORY';
            const hasTheory = cType === 'THEORY' || cType === 'INTEGRATED';
            const hasLab = cType === 'LABORATORY' || cType === 'INTEGRATED';
            
            let rowHtml = \`<tr id="row-\${e.id}">\`;
            rowHtml += \`<td><div style="display: flex; align-items: center;"><span class="modified-indicator"></span><strong>\${col1}</strong></div></td><td>\${col2}</td><td>\${col3}</td>\`;
            
            const renderInput = (field, val, max, enabled) => {
                if (!enabled) return \`<td><input type="number" class="spreadsheet-select-custom mark-input disabled-input" disabled title="N/A" value=""></td>\`;
                return \`<td><input type="number" class="spreadsheet-select-custom mark-input" data-field="\${field}" value="\${val ?? ''}" min="0" max="\${max}" oninput="trackGradeChange(\${e.id})"></td>\`;
            };

            if (isMixed || firstCourseType === 'THEORY' || firstCourseType === 'INTEGRATED') {
                rowHtml += renderInput('test1Marks', e.test1Marks, 30, hasTheory);
                rowHtml += renderInput('test2Marks', e.test2Marks, 30, hasTheory);
                rowHtml += renderInput('assignmentMarks', e.assignmentMarks, 10, hasTheory);
                rowHtml += renderInput('oaaMarks', e.oaaMarks, 10, hasTheory);
            }
            if (isMixed || firstCourseType === 'LABORATORY' || firstCourseType === 'INTEGRATED') {
                rowHtml += renderInput('regularLabMarks', e.regularLabMarks, 20, hasLab);
                rowHtml += renderInput('labTestMarks', e.labTestMarks, 20, hasLab);
                rowHtml += renderInput('labRecordMarks', e.labRecordMarks, 10, hasLab);
            }

            rowHtml += renderInput('seeMarks', e.seeMarks, 50, true);
            rowHtml += renderInput('graceMarks', e.graceMarks, 5, true);
            rowHtml += \`<td><span class="grade-badge \${e.grade || 'none'}">\${e.grade || 'Pending'}</span></td></tr>\`;
            
            return rowHtml;
          }).join('');
        }
        loading.style.display = 'none';`;

const startStr = '        let enrollments = [];';
const endStr = "        loading.style.display = 'none';";

const startIdx = code.indexOf(startStr);
const endIdx = code.indexOf(endStr) + endStr.length;

if (startIdx !== -1 && endIdx !== -1) {
    const newCode = code.substring(0, startIdx) + replacement + code.substring(endIdx);
    fs.writeFileSync('src/main/resources/META-INF/resources/js/events/facultyEvents.js', newCode);
    console.log('Successfully patched facultyEvents.js for dynamic spreadsheet generation.');
} else {
    console.error('Could not find start or end strings in facultyEvents.js');
}
