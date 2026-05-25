const fs = require('fs');

function replaceFile(path) {
    if (!fs.existsSync(path)) return;
    let content = fs.readFileSync(path, 'utf8');
    
    content = content.replace(/new EnrollmentResponse\(([^;]+)\)/g, (match, args) => {
        const parts = args.split(',');
        if (parts.length === 19) {
            parts.splice(8, 0, ' "THEORY"');
            parts.splice(17, 0, ' 0', ' 0', ' 0', ' 0', ' 0', ' 0', ' 0');
            return 'new EnrollmentResponse(' + parts.join(',') + ')';
        }
        return match;
    });

    fs.writeFileSync(path, content);
    console.log('Patched ' + path);
}

replaceFile('src/test/java/com/gradecalculator/controller/EnrollmentControllerTest.java');
replaceFile('src/test/java/com/gradecalculator/controller/FacultyGradeControllerTest.java');
