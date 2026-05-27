(function(){new MutationObserver((e,t)=>{document.body&&(s(),t.disconnect())}).observe(document.documentElement,{childList:!0,subtree:!0});function s(){if(document.getElementById("gp-page-loader"))return;const e=document.createElement("style");e.id="gp-loader-styles",e.textContent=`
            .page-loader {
                position: fixed;
                inset: 0;
                background: #faf9f7; /* Matches var(--paper) */
                z-index: 999999;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-direction: column;
                gap: 1.5rem;
                transition: opacity 0.4s cubic-bezier(0.16, 1, 0.3, 1), visibility 0.4s cubic-bezier(0.16, 1, 0.3, 1);
                opacity: 1;
                visibility: visible;
            }
            .page-loader::before {
                content: '';
                position: absolute;
                inset: 0;
                background: 
                    radial-gradient(circle at 10% 20%, rgba(139, 21, 56, 0.04) 0%, transparent 45%),
                    radial-gradient(circle at 90% 80%, rgba(201, 162, 39, 0.05) 0%, transparent 45%);
                pointer-events: none;
            }
            .page-loader.fade-out {
                opacity: 0;
                visibility: hidden;
            }
            .page-loader-spinner {
                width: 64px;
                height: 64px;
                border: 3.5px solid rgba(139, 21, 56, 0.08);
                border-top: 3.5px solid #8b1538; /* var(--crimson) */
                border-radius: 50%;
                animation: loader-spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
                position: relative;
            }
            .page-loader-spinner::after {
                content: '';
                position: absolute;
                inset: 6px;
                border: 3.5px solid rgba(201, 162, 39, 0.08);
                border-bottom: 3.5px solid #c9a227; /* var(--gold) */
                border-radius: 50%;
                animation: loader-spin-reverse 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
            }
            .page-loader-brand {
                font-family: 'Cormorant Garamond', Georgia, serif;
                font-size: 2.5rem;
                font-weight: 700;
                color: #8b1538;
                letter-spacing: -0.015em;
                animation: loader-pulse 2s ease-in-out infinite;
                z-index: 10;
                text-shadow: 0 2px 12px rgba(139, 21, 56, 0.04);
            }
            .page-loader-sub {
                font-size: 0.72rem;
                font-family: 'DM Sans', sans-serif;
                text-transform: uppercase;
                letter-spacing: 0.22em;
                color: #64748b;
                font-weight: 700;
                margin-top: -0.8rem;
                z-index: 10;
            }
            @keyframes loader-spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
            @keyframes loader-spin-reverse {
                0% { transform: rotate(360deg); }
                100% { transform: rotate(0deg); }
            }
            @keyframes loader-pulse {
                0%, 100% { opacity: 0.7; transform: scale(0.98); }
                50% { opacity: 1; transform: scale(1.02); }
            }
        `,document.head.appendChild(e);const t=document.createElement("div");t.id="gp-page-loader",t.className="page-loader",t.innerHTML=`
            <div class="page-loader-spinner"></div>
            <div class="page-loader-brand">GradePoint</div>
            <div class="page-loader-sub">Academic Excellence Tracker</div>
        `,document.body.insertBefore(t,document.body.firstChild)}window.hidePageLoader=function(){const e=document.getElementById("gp-page-loader");e&&!e.classList.contains("fade-out")&&(e.classList.add("fade-out"),setTimeout(()=>{e.remove();const t=document.getElementById("gp-loader-styles");t&&t.remove()},500))},setTimeout(()=>{window.hidePageLoader&&window.hidePageLoader()},1e3),document.addEventListener("DOMContentLoaded",()=>{if(!document.getElementById("toast-container")){const e=document.createElement("div");e.id="toast-container",e.className="toast-container",document.body.appendChild(e)}}),window.showToast=function(e,t="success"){const a=document.getElementById("toast-container");if(!a)return;const o=document.createElement("div");o.className=`toast toast-${t} show`,o.innerHTML=`
            <span>${t==="success"?"✨":"⚠️"}</span>
            <span>${e}</span>
        `,a.appendChild(o),setTimeout(()=>{o.classList.remove("show"),setTimeout(()=>o.remove(),400)},3e3)},window.logout=function(){localStorage.removeItem("token"),localStorage.removeItem("user"),window.location.href="/"},window.checkAuth=function(e=null){const t=localStorage.getItem("token"),a=localStorage.getItem("user"),o=window.location.pathname==="/"||window.location.pathname==="/index.html"||window.location.pathname==="/login.html";if(!t||!a)return o?setTimeout(()=>{window.hidePageLoader&&window.hidePageLoader()},50):window.location.href="/",null;try{const n=JSON.parse(a);return o?(n.role==="ADMIN"?window.location.href="/admin.html":n.role==="FACULTY"?window.location.href="/faculty-grades.html":window.location.href="/student.html",n):e&&n.role!==e?(n.role==="ADMIN"?window.location.href="/admin.html":n.role==="FACULTY"?window.location.href="/faculty-grades.html":window.location.href="/student.html",null):n}catch{return localStorage.removeItem("token"),localStorage.removeItem("user"),o?setTimeout(()=>{window.hidePageLoader&&window.hidePageLoader()},50):window.location.href="/",null}};const i=window.location.pathname;let r=null;i.includes("admin")?r="ADMIN":i.includes("faculty")?r="FACULTY":(i.includes("student")||i.includes("student-dashboard"))&&(r="STUDENT"),i!=="/home.html"&&window.checkAuth(r)})();
