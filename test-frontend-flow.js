const http = require('http');

let currentToken = null;

function request(method, path, data) {
    return new Promise((resolve, reject) => {
        const headers = { 'Content-Type': 'application/json' };
        if (currentToken) {
            headers['Authorization'] = 'Bearer ' + currentToken;
        }

        const options = {
            hostname: 'localhost',
            port: 8080,
            path: path,
            method: method,
            headers: headers
        };

        const req = http.request(options, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                let data = body;
                try {
                    data = body ? JSON.parse(body) : null;
                } catch (e) {}
                
                if (res.statusCode >= 400) {
                    reject(new Error(`API Error ${res.statusCode}: ${body}`));
                } else {
                    resolve({ status: res.statusCode, data: data });
                }
            });
        });

        req.on('error', reject);
        if (data) req.write(JSON.stringify(data));
        req.end();
    });
}

async function login(username, password) {
    const res = await request('POST', '/api/auth/login', { username, password });
    if (res.status === 200 && res.data.token) {
        currentToken = res.data.token;
        console.log(`Logged in as ${username}`);
    } else {
        throw new Error(`Login failed for ${username}: ${res.status}`);
    }
}

async function runTest() {
    try {
        console.log("--- STARTING E2E TEST ---");
        
        // 1. Admin Login
        await login('admin', 'admin');

        console.log("1. Get Semesters");
        const semRes = await request('GET', '/api/semesters');
        const semesterId = semRes.data[0].id;
        
        console.log("2. Get Faculty");
        const facRes = await request('GET', '/api/faculty/members');
        const profJones = facRes.data.find(f => f.username === 'prof.jones');
        const facultyId = profJones.id;

        console.log("3. Create INTEGRATED Course");
        const courseCode = 'CS' + Math.floor(Math.random() * 10000);
        const courseRes = await request('POST', '/api/courses', {
            courseCode: courseCode,
            courseName: 'Advanced Testing',
            credits: 4,
            semesterId: semesterId,
            facultyId: facultyId,
            courseType: 'INTEGRATED'
        });
        const courseId = courseRes.data.id;
        console.log("Course created:", courseId);

        console.log("4. Get Students");
        const stuRes = await request('GET', '/api/students');
        const studentId = stuRes.data[0].id;

        console.log("5. Create Enrollment");
        const enrRes = await request('POST', '/api/enrollments', {
            studentId: studentId,
            courseId: courseId
        });
        const enrollmentId = enrRes.data.id;
        console.log("Enrollment created:", enrollmentId);

        // 2. Faculty Login
        await login('prof.jones', 'password123');

        console.log("6. Update Grade (Marks) via Faculty API");
        const updateRes = await request('PUT', '/api/faculty/grades/bulk', [
            {
                enrollmentId: enrollmentId,
                test1Marks: 10,
                test2Marks: 10,
                assignmentMarks: 3,
                oaaMarks: 2,
                regularLabMarks: 10,
                labTestMarks: 8,
                labRecordMarks: 2,
                seeMarks: 45,
                graceMarks: 2
            }
        ]);
        console.log("Update status:", updateRes.status);

        console.log("7. Verify Final Grade");
        const verifyRes = await request('GET', `/api/enrollments/student/${studentId}`);
        const enrolled = verifyRes.data.find(e => e.id === enrollmentId);
        console.log("Final Record:", enrolled);
        
        if (enrolled && enrolled.grade === 'A+') {
            console.log("✅ TEST PASSED: Integration correctly calculated grade.");
        } else {
            console.log("❌ TEST FAILED: Grade calculation incorrect or not updated.", enrolled);
        }

    } catch (e) {
        console.error("Test failed:", e);
    }
}

runTest();
