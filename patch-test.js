const fs = require('fs');
let content = fs.readFileSync('src/test/java/com/gradecalculator/service/CourseServiceTest.java', 'utf8');

content = content.replace(/courseService\.create\(([^;]+)\)/g, (match, p1) => {
    // If it already has 6 arguments, leave it alone.
    if (p1.split(',').length >= 6) return match;
    return `courseService.create(${p1}, null)`;
});

content = content.replace(/courseService\.update\(([^;]+)\)/g, (match, p1) => {
    if (p1.split(',').length >= 7) return match;
    return `courseService.update(${p1}, null)`;
});

fs.writeFileSync('src/test/java/com/gradecalculator/service/CourseServiceTest.java', content);
