package com.gradecalculator.service;

import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByStudentIdAndSemesterId(Long studentId, Long semesterId) {
        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new IllegalArgumentException("This student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(student, course, LetterGrade.fromGrade(request.getGrade()));
        return toEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public Map<String, Object> update(Long id, EnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        enrollmentRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(id))
                .filter(existing -> existing.getStudent().getId().equals(student.getId()))
                .filter(existing -> existing.getCourse().getId().equals(course.getId()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This student is already enrolled in this course");
                });

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setGrade(LetterGrade.fromGrade(request.getGrade()));
        return toEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    public void delete(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
    }

    public Map<String, Object> toEnrollmentResponse(Enrollment enrollment) {
        Map<String, Object> response = new LinkedHashMap<>();
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();
        response.put("id", enrollment.getId());
        response.put("studentId", student.getId());
        response.put("studentName", student.getName());
        response.put("studentRollNumber", student.getStudentId());
        response.put("courseId", course.getId());
        response.put("courseCode", course.getCourseCode());
        response.put("courseName", course.getCourseName());
        response.put("credits", course.getCredits());
        response.put("semesterId", course.getSemester().getId());
        response.put("semesterNumber", course.getSemester().getSemesterNumber());
        response.put("grade", enrollment.getGrade().getGrade());
        response.put("gradePoints", enrollment.getGrade().getGradePoints());
        response.put("creditPoints", enrollment.getCreditPoints());
        return response;
    }
}
