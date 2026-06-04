import{apiService as v}from"./chunks/apiService.js";import{T as o,A as P,h}from"./chunks/errorHandler.js";import{S as w}from"./chunks/store.js";const I=function(){(function(){if(typeof window<"u"&&typeof window.DOMPurify>"u"){const l=document.createElement("script");l.src="https://cdnjs.cloudflare.com/ajax/libs/dompurify/3.0.8/purify.min.js",l.crossOrigin="anonymous",l.referrerPolicy="no-referrer",document.head.appendChild(l)}})(),function(){if(typeof window>"u")return;document.addEventListener("keydown",r=>{r.key==="Escape"&&document.querySelectorAll('.modal.active, [id*="modal"].active, [id*="Modal"].active').forEach(f=>{f.classList.remove("active")})});const l=document.createElement("style");l.id="accessibility-focus-style",l.textContent=`
            /* Focus visible accessibility states */
            button:focus-visible, input:focus-visible, select:focus-visible, a:focus-visible {
                outline: 3px solid #8b1538 !important;
                outline-offset: 2px !important;
                box-shadow: 0 0 0 4px rgba(139, 21, 56, 0.25) !important;
            }
        `,document.head.appendChild(l)}();function e(y){o.error(y)}function n(y){o.success(y)}function t(y,l="success"){o.show(y,l)}function s(y,l,r,f){const b=document.getElementById(y);if(!b)return;const B=b.value;b.innerHTML="",l.forEach(T=>{const H=document.createElement("option");H.value=T[r],H.textContent=T[f],b.appendChild(H)}),B&&(b.value=B)}function a(y,l){const r=document.createElement("tr");if(y.forEach(f=>{const b=document.createElement("td");b.textContent=f,r.appendChild(b)}),l){const f=document.createElement("td");f.innerHTML=l,r.appendChild(f)}return r}function d(y,l){var b;const r=((b=document.getElementById(l))==null?void 0:b.value.toLowerCase())||"";document.querySelectorAll(`#${y} tbody tr`).forEach(B=>{const T=B.textContent.toLowerCase();B.style.display=T.includes(r)?"":"none"})}function i(y,l){const r=document.getElementById(y);r&&(r.style.display=l?"":"none")}function c(y,l){y.forEach(r=>r.classList.remove("active")),l.classList.add("active")}function u(y,l="grid",r=3){const f=document.getElementById(y);if(!f)return;let b="";if(l==="table"){b=`
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
            `;for(let B=0;B<r;B++)b+=`
                    <tr class="skeleton-row">
                        <td><div class="skeleton-line short" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line medium" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line short" style="margin: 0.5rem 0;"></div></td>
                        <td><div class="skeleton-line long" style="margin: 0.5rem 0;"></div></td>
                    </tr>
                `;b+=`
                        </tbody>
                    </table>
                </div>
            `}else if(l==="profile")b=`
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
            `;else if(l==="list"){b=`
                <div class="skeleton-loader">
            `;for(let B=0;B<r;B++)b+=`
                    <div class="skeleton-card" style="padding: 1.25rem; margin-bottom: 0.75rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center; gap: 1rem;">
                            <div class="skeleton-line medium" style="margin: 0;"></div>
                            <div class="skeleton-line short" style="margin: 0;"></div>
                        </div>
                    </div>
                `;b+="</div>"}else{b=`
                <div class="skeleton-loader" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem;">
            `;for(let B=0;B<r;B++)b+=`
                    <div class="skeleton-card">
                        <div class="skeleton-line title"></div>
                        <div class="skeleton-line long"></div>
                        <div class="skeleton-line medium"></div>
                        <div class="skeleton-line short"></div>
                    </div>
                `;b+="</div>"}f.innerHTML=b}function E(y){u(y,"list",3)}function g(y,l){const r=document.getElementById(y);r&&(r.innerHTML=l||"")}function $(y){return new Date(y).toLocaleDateString("en-US",{year:"numeric",month:"short",day:"numeric"})}function m(y,l){let r;return function(...b){const B=()=>{clearTimeout(r),y(...b)};clearTimeout(r),r=setTimeout(B,l)}}function L(y){return y==null?"":typeof window<"u"&&window.DOMPurify?window.DOMPurify.sanitize(String(y)):String(y).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#39;")}return{escapeHTML:L,showError:e,showSuccess:n,showToast:t,populateSelect:s,createTableRow:a,filterTable:d,toggleSection:i,setActiveTab:c,showSkeletonLoader:u,showLoading:E,hideLoading:g,formatDate:$,debounce:m}}();function O(e,n){if(e.innerHTML="",!Array.isArray(n)||n.length===0){const t=document.createElement("tr"),s=document.createElement("td");s.colSpan=6,s.textContent="No student records found.",t.appendChild(s),e.appendChild(t);return}n.forEach(t=>{const s=`
      <button class="btn btn-secondary btn-sm edit-student-btn" style="margin-right: 0.5rem;" data-id="${t.id}" data-name="${t.name}" data-roll="${t.studentId}" data-branch="${t.branch}" data-dob="${t.dateOfBirth||""}">Edit</button>
      <button class="btn btn-danger btn-sm delete-student-btn" style="margin-right: 0.5rem;" data-id="${t.id}">Delete</button>
      <button class="btn btn-warning btn-sm change-pass-student-btn" data-username="${t.username}">Change Password</button>
    `,a=I.createTableRow([t.id,t.name,t.studentId,t.branch,"••••"],s);a.classList.add("student-row-item"),a.setAttribute("data-name",t.name.toLowerCase()),a.setAttribute("data-roll",t.studentId.toLowerCase()),e.appendChild(a)})}const M={open:(e,n="")=>{const t=document.getElementById(e);t&&(n&&(t.dataset.username=n),t.classList.add("active"))},close:e=>{const n=document.getElementById(e);if(n){n.classList.remove("active");const t=document.getElementById("newPasswordInput");t&&(t.value="")}}};window.Modal=M;function F(e){M.open("passwordModal",e)}function R(){M.close("passwordModal")}function j(){const e=document.getElementById("passwordModal").dataset.username,n=document.getElementById("newPasswordInput").value.trim();if(!n){o.error("Password cannot be empty");return}v.changeUserPassword(e,n).then(()=>{o.success("Password changed successfully"),R()}).catch(t=>{h(t,"change password")})}P.requireRole("ADMIN");function V(e,n){document.querySelectorAll(".tab-content").forEach(t=>t.classList.remove("active")),document.querySelectorAll(".sidebar-item").forEach(t=>t.classList.remove("active")),document.getElementById(`tab-${e}`).classList.add("active"),n.classList.add("active")}async function U(){const e=P.getUser();if(e){const n=document.getElementById("nav-username"),t=document.getElementById("nav-role");n&&(n.textContent=e.username),t&&(t.textContent=e.role)}G(),Z(),await Y(),window.hidePageLoader&&window.hidePageLoader()}function G(){document.querySelectorAll(".sidebar-item").forEach(l=>{l.addEventListener("click",()=>{const r=l.dataset.tab;r&&V(r,l)})});const e=document.getElementById("btn-create-student");e&&e.addEventListener("click",W);const n=document.getElementById("btn-create-semester");n&&n.addEventListener("click",Q);const t=document.getElementById("btn-create-course");t&&t.addEventListener("click",X);const s=document.getElementById("btn-create-enrollment");s&&s.addEventListener("click",_);const a=document.getElementById("btn-create-faculty");a&&a.addEventListener("click",J);const d=document.getElementById("btn-create-department");d&&d.addEventListener("click",K),["students","courses","enrollments","faculty","departments"].forEach(l=>{const r=document.getElementById(`search-${l}`);if(r){const f=I.debounce(()=>z(l),250);r.addEventListener("input",f)}});const i=document.getElementById("grades-student");i&&i.addEventListener("change",le),document.querySelectorAll(".close-password-modal-btn, .cancel-password-modal-btn").forEach(l=>{l.addEventListener("click",R)});const c=document.querySelector(".submit-password-modal-btn");c&&c.addEventListener("click",j),document.querySelectorAll(".close-edit-modal-btn, .cancel-edit-modal-btn").forEach(l=>{l.addEventListener("click",S)});const u=document.querySelector(".submit-edit-modal-btn");u&&u.addEventListener("click",ye);const E=document.querySelector("#students-table tbody");E&&E.addEventListener("click",l=>{const r=l.target.closest(".edit-student-btn"),f=l.target.closest(".delete-student-btn"),b=l.target.closest(".change-pass-student-btn");r?re(r.dataset.id,r.dataset.name,r.dataset.roll,r.dataset.branch,r.dataset.dob):f?ee(f.dataset.id):b&&F(b.dataset.username)});const g=document.querySelector("#semesters-table tbody");g&&g.addEventListener("click",l=>{const r=l.target.closest(".edit-semester-btn"),f=l.target.closest(".delete-semester-btn");r?oe(r.dataset.id,r.dataset.number):f&&ne(f.dataset.id)});const $=document.querySelector("#courses-table tbody");$&&$.addEventListener("click",l=>{const r=l.target.closest(".edit-course-btn"),f=l.target.closest(".delete-course-btn");r?ie(r.dataset.id,r.dataset.code,r.dataset.name,r.dataset.credits,r.dataset.semesterId,r.dataset.facultyId,r.dataset.type):f&&ae(f.dataset.id)});const m=document.querySelector("#enrollments-table tbody");m&&m.addEventListener("click",l=>{const r=l.target.closest(".edit-enrollment-btn"),f=l.target.closest(".delete-enrollment-btn");r?me(r.dataset.id,r.dataset.studentId,r.dataset.courseId,r.dataset.cieMarks,r.dataset.cieTheoryMarks,r.dataset.cieLabMarks,r.dataset.seeMarks,r.dataset.graceMarks):f&&de(f.dataset.id)});const L=document.querySelector("#faculty-table tbody");L&&L.addEventListener("click",l=>{const r=l.target.closest(".edit-faculty-btn"),f=l.target.closest(".delete-faculty-btn"),b=l.target.closest(".change-pass-faculty-btn");r?ce(r.dataset.id,r.dataset.name,r.dataset.username,r.dataset.email,r.dataset.department):f?te(f.dataset.id):b&&F(b.dataset.username)});const y=document.querySelector("#departments-table tbody");y&&y.addEventListener("click",l=>{const r=l.target.closest(".edit-department-btn"),f=l.target.closest(".delete-department-btn");r?ue(r.dataset.id,r.dataset.name,r.dataset.code):f&&se(f.dataset.id)})}async function Y(){await N(),await k(),await x(),await A(),await D(),await q(),document.getElementById("count-students").textContent=(w.get("students")||[]).length,document.getElementById("count-courses").textContent=(w.get("courses")||[]).length,document.getElementById("count-enrollments").textContent=(w.get("enrollments")||[]).length}async function k(){try{const e=await v.getStudents();w.set("students",e);const n=document.querySelector("#students-table tbody");document.getElementById("count-students-text").textContent=`${e.length} profile files siphoned`,O(n,e);const t=document.getElementById("enrollment-student"),s=document.getElementById("grades-student"),a=e.map(d=>`<option value="${d.id}">${d.name} (${d.studentId})</option>`).join("");t.innerHTML='<option value="">Select student target...</option>'+a,s.innerHTML='<option value="">Select student file...</option>'+a}catch(e){h(e,"load students"),o.error("Failed to pull student accounts.")}}async function x(){try{const e=await v.getSemesters();w.set("semesters",e),document.getElementById("count-semesters").textContent=e.length;const n=document.querySelector("#semesters-table tbody");n.innerHTML=e.length?e.map(s=>`
            <tr>
                <td><strong>${s.id}</strong></td>
                <td>Semester Stage ${s.semesterNumber}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-semester-btn" style="margin-right: 0.5rem;" data-id="${s.id}" data-number="${s.semesterNumber}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-semester-btn" data-id="${s.id}">Delete</button>
                </td>
            </tr>
        `).join(""):'<tr><td colspan="3" class="empty-state">No semester cycles defined.</td></tr>';const t=document.getElementById("course-semester");t.innerHTML=e.map(s=>`<option value="${s.id}">Semester Stage ${s.semesterNumber}</option>`).join("")}catch(e){h(e,"load semesters"),o.error("Failed to load semesters.")}}async function A(){try{const e=await v.getCourses();w.set("courses",e);const n=document.querySelector("#courses-table tbody");n.innerHTML=e.length?e.map(s=>{var a,d,i,c;return`
            <tr class="course-row-item" data-code="${s.code.toLowerCase()}" data-name="${s.name.toLowerCase()}">
                <td><strong>${s.code}</strong></td>
                <td>${s.name}</td>
                <td>${s.credits} Credits<br><small style="color:var(--slate-light)">${s.courseType||"THEORY"}</small></td>
                <td><span class="grade-badge O">Sem ${((a=s.semester)==null?void 0:a.semesterNumber)||"-"}</span></td>
                <td>${((d=s.faculty)==null?void 0:d.name)||"-"}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-course-btn" style="margin-right: 0.5rem;" 
                        data-id="${s.id}" 
                        data-code="${I.escapeHTML(s.code)}" 
                        data-name="${I.escapeHTML(s.name)}" 
                        data-credits="${s.credits}" 
                        data-semester-id="${((i=s.semester)==null?void 0:i.id)||0}" 
                        data-faculty-id="${((c=s.faculty)==null?void 0:c.id)||0}" 
                        data-type="${s.courseType||"THEORY"}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-course-btn" data-id="${s.id}">Delete</button>
                </td>
            </tr>
        `}).join(""):'<tr><td colspan="6" class="empty-state">Curriculum inventory is empty.</td></tr>';const t=document.getElementById("enrollment-course");t.innerHTML='<option value="">Select syllabus course...</option>'+e.map(s=>`<option value="${s.id}">${s.code} - ${s.name}</option>`).join("")}catch(e){h(e,"load courses"),o.error("Failed to pull curriculum list.")}}async function D(){try{const e=await v.getFacultyMembers();w.set("faculty",e);const n=document.getElementById("course-faculty");n.innerHTML='<option value="">Select faculty...</option>'+e.map(s=>`<option value="${s.id}">${s.name}</option>`).join("");const t=document.querySelector("#faculty-table tbody");t.innerHTML=e.length?e.map(s=>{var a;return`
            <tr class="faculty-row-item" data-name="${s.name.toLowerCase()}" data-username="${((a=s.username)==null?void 0:a.toLowerCase())||""}">
                <td><strong>${s.id}</strong></td>
                <td>${s.name}</td>
                <td>${s.username}</td>
                <td><code>••••</code></td>
                <td>${s.email||"-"}</td>
                <td>${s.department||"-"}</td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-faculty-btn" style="margin-right: 0.5rem;" 
                        data-id="${s.id}" 
                        data-name="${I.escapeHTML(s.name)}" 
                        data-username="${I.escapeHTML(s.username)}" 
                        data-email="${I.escapeHTML(s.email||"")}" 
                        data-department="${I.escapeHTML(s.department||"")}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-faculty-btn" style="margin-right: 0.5rem;" data-id="${s.id}">Delete</button>
                    <button class="btn btn-warning btn-sm change-pass-faculty-btn" data-username="${I.escapeHTML(s.username)}">Change Password</button>
                </td>
            </tr>
        `}).join(""):'<tr><td colspan="7" class="empty-state">No faculty accounts registered.</td></tr>',document.getElementById("count-faculty-text").textContent=`${e.length} faculty members loaded`}catch(e){h(e,"load faculty list"),o.error("Failed to load faculty list.")}}async function N(){try{const e=await v.getDepartments();w.set("departments",e),document.getElementById("count-departments-text").textContent=`${e.length} departments loaded`;const n=document.querySelector("#departments-table tbody");n.innerHTML=e.length?e.map(d=>`
            <tr class="department-row-item" data-name="${d.name.toLowerCase()}" data-code="${d.code.toLowerCase()}">
                <td><strong>${d.id}</strong></td>
                <td>${d.name}</td>
                <td><span class="grade-badge A">${d.code}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-department-btn" style="margin-right: 0.5rem;" 
                        data-id="${d.id}" 
                        data-name="${I.escapeHTML(d.name)}" 
                        data-code="${I.escapeHTML(d.code)}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-department-btn" data-id="${d.id}">Delete</button>
                </td>
            </tr>
        `).join(""):'<tr><td colspan="4" class="empty-state">No departments registered.</td></tr>';const t=document.getElementById("student-branch"),s=document.getElementById("faculty-department"),a=e.map(d=>`<option value="${d.name}">${d.name}</option>`).join("");t.innerHTML=a,s.innerHTML=a}catch(e){h(e,"pull departments"),o.error("Failed to pull departments.")}}async function q(){try{const e=await v.getEnrollments();w.set("enrollments",e);const n=document.querySelector("#enrollments-table tbody");n.innerHTML=e.length?e.map(t=>`
            <tr class="enroll-row-item" data-student="${(t.studentName||"").toLowerCase()}" data-code="${(t.courseCode||"").toLowerCase()}">
                <td><strong>${t.studentName||"N/A"}</strong></td>
                <td>${t.courseCode||"N/A"} - ${t.courseName||"N/A"}</td>
                <td><span class="grade-badge ${t.grade||"none"}">${t.grade||"Pending"}</span></td>
                <td style="text-align: right;">
                    <button class="btn btn-secondary btn-sm edit-enrollment-btn" style="margin-right: 0.5rem;" 
                        data-id="${t.id}" 
                        data-student-id="${t.studentId||0}" 
                        data-course-id="${t.courseId||0}" 
                        data-cie-marks="${t.cieMarks||0}" 
                        data-cie-theory-marks="${t.cieTheoryMarks||0}" 
                        data-cie-lab-marks="${t.cieLabMarks||0}" 
                        data-see-marks="${t.seeMarks||0}" 
                        data-grace-marks="${t.graceMarks||0}">Edit</button>
                    <button class="btn btn-danger btn-sm delete-enrollment-btn" data-id="${t.id}">Delete</button>
                </td>
            </tr>
        `).join(""):'<tr><td colspan="4" class="empty-state">No course registrations active.</td></tr>'}catch(e){h(e,"load system registrations"),o.error("Failed to load system registrations.")}}function z(e){const n=document.getElementById(`search-${e}`).value.toLowerCase().trim();e==="students"?document.querySelectorAll(".student-row-item").forEach(t=>{const s=t.getAttribute("data-name")||"",a=t.getAttribute("data-roll")||"";t.style.display=s.includes(n)||a.includes(n)?"":"none"}):e==="courses"?document.querySelectorAll(".course-row-item").forEach(t=>{const s=t.getAttribute("data-name")||"",a=t.getAttribute("data-code")||"";t.style.display=s.includes(n)||a.includes(n)?"":"none"}):e==="enrollments"?document.querySelectorAll(".enroll-row-item").forEach(t=>{const s=t.getAttribute("data-student")||"",a=t.getAttribute("data-code")||"";t.style.display=s.includes(n)||a.includes(n)?"":"none"}):e==="faculty"?document.querySelectorAll(".faculty-row-item").forEach(t=>{const s=t.getAttribute("data-name")||"",a=t.getAttribute("data-username")||"";t.style.display=s.includes(n)||a.includes(n)?"":"none"}):e==="departments"&&document.querySelectorAll(".department-row-item").forEach(t=>{const s=t.getAttribute("data-name")||"",a=t.getAttribute("data-code")||"";t.style.display=s.includes(n)||a.includes(n)?"":"none"})}function p(e,n,t=""){if(e)if(n){e.classList.remove("is-invalid"),e.classList.add("is-valid");const s=e.parentNode.querySelector(".validation-feedback");s&&s.remove()}else{e.classList.remove("is-valid"),e.classList.add("is-invalid");let s=e.parentNode.querySelector(".validation-feedback");s||(s=document.createElement("div"),s.className="validation-feedback",e.parentNode.appendChild(s)),s.textContent=t}}function C(e){const n=document.getElementById(e)||document;n.querySelectorAll(".is-valid, .is-invalid").forEach(t=>{t.classList.remove("is-valid","is-invalid")}),n.querySelectorAll(".validation-feedback").forEach(t=>{t.remove()})}function Z(){const e=document.getElementById("student-name");e&&e.addEventListener("input",()=>{const m=e.value.trim();m?m.length<2?p(e,!1,"Name must be at least 2 characters"):p(e,!0):p(e,!1,"Student name is required")});const n=document.getElementById("student-id");n&&n.addEventListener("input",()=>{const m=n.value.trim();m?/^[A-Za-z0-9]{3,15}$/.test(m)?p(n,!0):p(n,!1,"ID must be 3-15 alphanumeric characters"):p(n,!1,"Student ID is required")});const t=document.getElementById("student-dob");t&&t.addEventListener("change",()=>{const m=t.value;m?new Date(m)>=new Date?p(t,!1,"Date of Birth must be in the past"):p(t,!0):p(t,!1,"Date of Birth is required")});const s=document.getElementById("faculty-name");s&&s.addEventListener("input",()=>{s.value.trim()?p(s,!0):p(s,!1,"Faculty name is required")});const a=document.getElementById("faculty-username");a&&a.addEventListener("input",()=>{const m=a.value.trim();m?m.length<3?p(a,!1,"Username must be at least 3 characters"):p(a,!0):p(a,!1,"Username is required")});const d=document.getElementById("faculty-email");d&&d.addEventListener("input",()=>{const m=d.value.trim();m?/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(m)?p(d,!0):p(d,!1,"Provide a valid email format"):p(d,!1,"Email address is required")});const i=document.getElementById("department-name");i&&i.addEventListener("input",()=>{i.value.trim()?p(i,!0):p(i,!1,"Department name is required")});const c=document.getElementById("department-code");c&&c.addEventListener("input",()=>{const m=c.value.trim();m?/^[A-Za-z0-9]{2,5}$/.test(m)?p(c,!0):p(c,!1,"Code must be 2-5 alphanumeric characters"):p(c,!1,"Department code is required")});const u=document.getElementById("semester-number");u&&u.addEventListener("input",()=>{const m=parseInt(u.value.trim());isNaN(m)||m<1||m>10?p(u,!1,"Semester sequence must be between 1 and 10"):p(u,!0)});const E=document.getElementById("course-code");E&&E.addEventListener("input",()=>{const m=E.value.trim();m?/^[A-Za-z0-9-]{3,10}$/.test(m)?p(E,!0):p(E,!1,"Code must be 3-10 alphanumeric/hyphen characters"):p(E,!1,"Course Code is required")});const g=document.getElementById("course-name");g&&g.addEventListener("input",()=>{const m=g.value.trim();m?m.length<3?p(g,!1,"Title must be at least 3 characters"):p(g,!0):p(g,!1,"Course Title is required")});const $=document.getElementById("course-credits");$&&$.addEventListener("input",()=>{const m=parseInt($.value.trim());isNaN(m)||m<1||m>6?p($,!1,"Credits must be between 1 and 6"):p($,!0)})}async function W(){const e=document.getElementById("student-name"),n=document.getElementById("student-id"),t=document.getElementById("student-dob"),s=document.getElementById("student-branch");if(e.dispatchEvent(new Event("input")),n.dispatchEvent(new Event("input")),t.dispatchEvent(new Event("change")),e.classList.contains("is-invalid")||n.classList.contains("is-invalid")||t.classList.contains("is-invalid")){o.error("Please fix validation errors first.");return}const a=e.value.trim(),d=n.value.trim(),i=s.value,c=t.value;if(!a||!d||!c){o.error("Complete all floating fields.");return}try{await v.createStudent(a,d,i,c),e.value="",n.value="",t.value="2004-01-01",C("tab-students"),await k(),o.success(`Student profile '${a}' registered successfully!`)}catch(u){h(u,"failed record"),o.error("Failed to register student record.")}}async function J(){const e=document.getElementById("faculty-name"),n=document.getElementById("faculty-username"),t=document.getElementById("faculty-email"),s=document.getElementById("faculty-department");if(e.dispatchEvent(new Event("input")),n.dispatchEvent(new Event("input")),t.dispatchEvent(new Event("input")),e.classList.contains("is-invalid")||n.classList.contains("is-invalid")||t.classList.contains("is-invalid")){o.error("Please fix validation errors first.");return}const a=e.value.trim(),d=n.value.trim(),i=t.value.trim(),c=s.value.trim();if(!a||!d||!i||!c){o.error("Complete all faculty fields.");return}try{const u=prompt("Enter a password for the new faculty member:","password123");if(u===null)return;if(!u.trim()){o.error("Password cannot be empty.");return}await v.createFaculty(a,d,u.trim(),i,c),e.value="",n.value="",t.value="",C("tab-faculty"),await D(),o.success(`Faculty staff '${a}' onboarded successfully!`)}catch(u){h(u,"failed staff"),o.error("Failed to onboard faculty staff.")}}async function K(){const e=document.getElementById("department-name"),n=document.getElementById("department-code");if(e.dispatchEvent(new Event("input")),n.dispatchEvent(new Event("input")),e.classList.contains("is-invalid")||n.classList.contains("is-invalid")){o.error("Please fix validation errors first.");return}const t=e.value.trim(),s=n.value.trim();if(!t||!s){o.error("Complete all department fields.");return}try{await v.createDepartment(t,s),e.value="",n.value="",C("tab-departments"),await N(),o.success(`Department '${t}' created successfully!`)}catch(a){h(a,"failed Duplicates?"),o.error("Failed to create department. Duplicates?")}}async function Q(){const e=document.getElementById("semester-number");if(e.dispatchEvent(new Event("input")),e.classList.contains("is-invalid")){o.error("Please fix validation errors first.");return}const n=e.value.trim();if(!n){o.error("Provide a semester number cycle.");return}try{await v.createSemester(parseInt(n)),e.value="",C("tab-semesters"),await x(),o.success(`Academic Semester Stage ${n} established!`)}catch(t){h(t,"failed stage"),o.error("Failed to establish semester stage.")}}async function X(){const e=document.getElementById("course-code"),n=document.getElementById("course-name"),t=document.getElementById("course-credits"),s=document.getElementById("course-semester"),a=document.getElementById("course-faculty"),d=document.getElementById("course-type");if(e.dispatchEvent(new Event("input")),n.dispatchEvent(new Event("input")),t.dispatchEvent(new Event("input")),e.classList.contains("is-invalid")||n.classList.contains("is-invalid")||t.classList.contains("is-invalid")){o.error("Please fix validation errors first.");return}const i=e.value.trim(),c=n.value.trim(),u=t.value.trim(),E=s.value,g=a.value,$=d.value;if(!i||!c||!u||!E||!g){o.error("Complete all course fields.");return}try{await v.createCourse(i,c,parseInt(u),E,g,$),e.value="",n.value="",t.value="",C("tab-courses"),await A(),o.success(`Syllabus item '${i}: ${c}' created successfully.`)}catch(m){h(m,"failed item"),o.error("Failed to create syllabus course item.")}}async function _(){const e=document.getElementById("enrollment-student").value,n=document.getElementById("enrollment-course").value;if(!e||!n){o.error("Choose student target and course mapping.");return}try{await v.createEnrollment(e,n),await q(),o.success("Course registration siphoned successfully.")}catch(t){h(t,"failed Duplicates?"),o.error("Failed to map student registration. Duplicates?")}}async function ee(e){if(confirm("Verify: Permanently purge student record and related grades files?"))try{await v.deleteStudent(e),await k(),o.success("Student file successfully purged.")}catch{o.error("Purge rejected by data locks.")}}async function te(e){if(confirm("Verify: Permanently purge faculty member account?"))try{await v.deleteFaculty(e),await D(),o.success("Faculty member successfully purged.")}catch(n){h(n,"delete faculty member"),o.error("Failed to delete faculty member.")}}async function se(e){if(confirm("Verify: Permanently delete this department?"))try{await v.deleteDepartment(e),await N(),o.success("Department successfully deleted.")}catch{o.error("Failed to delete department.")}}async function ne(e){if(confirm("Verify: Purge semester cycle and connected syllabus records?"))try{await v.deleteSemester(e),await x(),o.success("Semester sequence successfully deleted.")}catch{o.error("Purge rejected: dependent courses exist.")}}async function ae(e){if(confirm("Verify: Purge course syllabus item?"))try{await v.deleteCourse(e),await A(),o.success("Syllabus course successfully deleted.")}catch{o.error("Purge rejected: Active class student enrollments exist.")}}async function de(e){if(confirm("Verify: Revoke class course registration?"))try{await v.deleteEnrollment(e),await q(),o.success("Class registration successfully revoked.")}catch{o.error("Revoke action rejected.")}}async function le(){const e=document.getElementById("grades-student").value,n=document.getElementById("grades-result");if(!e){n.style.display="none";return}try{const t=await v.getCGPA(e),s=(w.get("students")||[]).find(c=>c.id==e),a=t.cgpa||0;let d="Academic Warning",i="F";a>=9?(d="First Class with Distinction",i="O"):a>=8?(d="First Class",i="A\\+"):a>=7?(d="Second Class (Upper Division)",i="A"):a>=6?(d="Second Class (Lower Division)",i="B\\+"):a>=5?(d="Third Class",i="B"):a>=4&&(d="Pass",i="C"),n.innerHTML=`
            <div class="stat-card-luxury" style="text-align: left; padding: 2rem;">
                <h4 style="font-family: 'Cormorant Garamond', serif; font-size: 1.6rem; color: var(--crimson); margin-bottom: 1rem;">
                    Academic Status: ${(s==null?void 0:s.name)||"File"}
                </h4>
                <div class="form-grid" style="grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                    <div>
                        <span class="stat-title-lux">Overall CGPA</span>
                        <div class="stat-value-lux" style="font-size: 2.2rem; color: var(--gold-dark);">${(t.cgpa||0).toFixed(2)}</div>
                    </div>
                    <div>
                        <span class="stat-title-lux">Academic Standing</span>
                        <div style="margin-top: 0.5rem;">
                            <span class="grade-badge ${i}">${d}</span>
                        </div>
                    </div>
                </div>
            </div>
        `,n.style.display="block"}catch{o.error("Failed to pull student audit ledger."),n.style.display="none"}}function S(){M.close("editModal")}function re(e,n,t,s,a){const d=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Student Profile";const c=(w.get("departments")||[]).map(u=>u.name).map(u=>`<option value="${u}" ${u===s?"selected":""}>${u}</option>`).join("");document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Full Name</label>
            <input type="text" class="form-input" id="edit-student-name" value="${I.escapeHTML(n)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic ID / Roll Number</label>
            <input type="text" class="form-input" id="edit-student-roll" value="${I.escapeHTML(t)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic Branch</label>
            <select class="form-select" id="edit-student-branch">
                ${c}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Date of Birth</label>
            <input type="date" class="form-input" id="edit-student-dob" value="${a}">
        </div>
    `,d.dataset.type="student",d.dataset.id=e,d.classList.add("active")}function oe(e,n){const t=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Semester Stage",document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Semester Number Sequence</label>
            <input type="number" class="form-input" id="edit-semester-number" value="${n}" min="1" max="10">
        </div>
    `,t.dataset.type="semester",t.dataset.id=e,t.classList.add("active")}function ie(e,n,t,s,a,d,i){const c=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Syllabus Course";const u=(w.get("semesters")||[]).map(g=>`<option value="${g.id}" ${g.id==a?"selected":""}>Semester Stage ${g.semesterNumber}</option>`).join(""),E=(w.get("faculty")||[]).map(g=>`<option value="${g.id}" ${g.id==d?"selected":""}>${g.name}</option>`).join("");document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Code</label>
            <input type="text" class="form-input" id="edit-course-code" value="${I.escapeHTML(n)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Title</label>
            <input type="text" class="form-input" id="edit-course-name" value="${I.escapeHTML(t)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Academic Credits Value</label>
            <input type="number" class="form-input" id="edit-course-credits" value="${s}" min="1" max="6">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Semester Mapping</label>
            <select class="form-select" id="edit-course-semester">
                ${u}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Faculty Instructor</label>
            <select class="form-select" id="edit-course-faculty">
                ${E}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Course Type</label>
            <select class="form-select" id="edit-course-type">
                <option value="THEORY" ${i==="THEORY"?"selected":""}>Theory</option>
                <option value="LABORATORY" ${i==="LABORATORY"?"selected":""}>Laboratory</option>
                <option value="INTEGRATED" ${i==="INTEGRATED"?"selected":""}>Integrated (Theory + Lab)</option>
            </select>
        </div>
    `,c.dataset.type="course",c.dataset.id=e,c.classList.add("active")}function ce(e,n,t,s,a){const d=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Faculty Profile";const c=(w.get("departments")||[]).map(u=>u.name).map(u=>`<option value="${u}" ${u===a?"selected":""}>${u}</option>`).join("");document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Faculty Name</label>
            <input type="text" class="form-input" id="edit-faculty-name" value="${I.escapeHTML(n)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Username</label>
            <input type="text" class="form-input" id="edit-faculty-username" value="${I.escapeHTML(t)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Email</label>
            <input type="email" class="form-input" id="edit-faculty-email" value="${I.escapeHTML(s)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department</label>
            <select class="form-select" id="edit-faculty-department">
                ${c}
            </select>
        </div>
    `,d.dataset.type="faculty",d.dataset.id=e,d.classList.add("active")}function ue(e,n,t){const s=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Department",document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department Name</label>
            <input type="text" class="form-input" id="edit-department-name" value="${I.escapeHTML(n)}">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Department Code</label>
            <input type="text" class="form-input" id="edit-department-code" value="${I.escapeHTML(t)}">
        </div>
    `,s.dataset.type="department",s.dataset.id=e,s.classList.add("active")}function me(e,n,t,s,a,d,i,c){const u=document.getElementById("editModal");document.getElementById("editModalTitle").textContent="Edit Course Enrollment";const E=(w.get("students")||[]).map(l=>`<option value="${l.id}" ${l.id==n?"selected":""}>${l.name} (${l.studentId})</option>`).join(""),$=(w.get("courses")||[]).map(l=>`<option value="${l.id}" ${l.id==t?"selected":""}>${l.code} - ${l.name}</option>`).join(""),m=(w.get("courses")||[]).find(l=>l.id==t),L=m&&m.courseType||"THEORY";let y="";L==="INTEGRATED"?y=`
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Theory Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie-theory" value="${a}" min="0" max="50">
            </div>
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Lab Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie-lab" value="${d}" min="0" max="50">
            </div>
        `:y=`
            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label">CIE Marks</label>
                <input type="number" class="form-input" id="edit-enrollment-cie" value="${s}" min="0" max="50">
            </div>
        `,y+=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">SEE Marks</label>
            <input type="number" class="form-input" id="edit-enrollment-see" value="${i}" min="0" max="50">
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Grace Marks</label>
            <input type="number" class="form-input" id="edit-enrollment-grace" value="${c}" min="0" max="5">
        </div>
    `,document.getElementById("editModalBody").innerHTML=`
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Select Student Record</label>
            <select class="form-select" id="edit-enrollment-student" disabled>
                ${E}
            </select>
        </div>
        <div class="form-group" style="margin-bottom: 1.25rem;">
            <label class="form-label">Select Syllabus Course</label>
            <select class="form-select" id="edit-enrollment-course" disabled>
                ${$}
            </select>
        </div>
        ${y}
    `,u.dataset.type="enrollment",u.dataset.id=e,u.dataset.courseType=L,u.classList.add("active")}async function ye(){const e=document.getElementById("editModal"),n=e.dataset.type,t=e.dataset.id;if(n==="student"){const s=document.getElementById("edit-student-name").value.trim(),a=document.getElementById("edit-student-roll").value.trim(),d=document.getElementById("edit-student-branch").value,i=document.getElementById("edit-student-dob").value;if(!s||!a||!d||!i){o.error("All student fields are required.");return}try{await v.updateStudent(t,s,a,d,i),await k(),o.success("Student file successfully modified."),S()}catch(c){h(c,"edit student"),o.error("Failed to edit student profile.")}}else if(n==="semester"){const s=document.getElementById("edit-semester-number").value.trim(),a=parseInt(s);if(isNaN(a)||a<1||a>10){o.error("Semester number must be an integer between 1 and 10.");return}try{await v.updateSemester(t,a),await x(),o.success(`Semester successfully updated to Stage ${a}!`),S()}catch(d){h(d,"edit semester"),o.error("Failed to modify semester sequence.")}}else if(n==="course"){const s=document.getElementById("edit-course-code").value.trim(),a=document.getElementById("edit-course-name").value.trim(),d=document.getElementById("edit-course-credits").value.trim(),i=parseInt(document.getElementById("edit-course-semester").value),c=parseInt(document.getElementById("edit-course-faculty").value),u=document.getElementById("edit-course-type").value,E=parseInt(d);if(isNaN(E)||E<1||E>6){o.error("Credits must be an integer between 1 and 6.");return}if(!s||!a||isNaN(i)||isNaN(c)){o.error("All course fields are required.");return}try{await v.updateCourse(t,s,a,E,i,c,u),await A(),o.success(`Syllabus course '${s}' modified successfully!`),S()}catch(g){h(g,"edit course"),o.error("Failed to update syllabus course item.")}}else if(n==="faculty"){const s=document.getElementById("edit-faculty-name").value.trim(),a=document.getElementById("edit-faculty-username").value.trim(),d=document.getElementById("edit-faculty-email").value.trim(),i=document.getElementById("edit-faculty-department").value;if(!s||!a||!d||!i){o.error("All faculty fields are required.");return}try{await v.updateFaculty(t,s,a,d,i),await D(),o.success(`Faculty profile '${a}' modified successfully!`),S()}catch(c){h(c,"edit faculty"),o.error("Failed to modify faculty record.")}}else if(n==="department"){const s=document.getElementById("edit-department-name").value.trim(),a=document.getElementById("edit-department-code").value.trim();if(!s||!a){o.error("Department name and code cannot be empty.");return}try{await v.updateDepartment(t,s,a.toUpperCase()),await N(),o.success(`Department successfully modified to '${s}'!`),S()}catch(d){h(d,"edit department"),o.error("Failed to update department record.")}}else if(n==="enrollment"){const s=parseInt(document.getElementById("edit-enrollment-student").value),a=parseInt(document.getElementById("edit-enrollment-course").value);if(isNaN(s)||isNaN(a)){o.error("Student and Course selections are required.");return}const d=e.dataset.courseType;let i=0,c=0,u=0;d==="INTEGRATED"?(c=parseInt(document.getElementById("edit-enrollment-cie-theory").value)||0,u=parseInt(document.getElementById("edit-enrollment-cie-lab").value)||0):i=parseInt(document.getElementById("edit-enrollment-cie").value)||0;const E=parseInt(document.getElementById("edit-enrollment-see").value)||0,g=parseInt(document.getElementById("edit-enrollment-grace").value)||0;try{await v.updateEnrollment(t,s,a,i,c,u,E,g),await q(),o.success("Course enrollment successfully updated!"),S()}catch($){h($,"edit enrollment"),o.error("Failed to update course enrollment marks.")}}}U();
