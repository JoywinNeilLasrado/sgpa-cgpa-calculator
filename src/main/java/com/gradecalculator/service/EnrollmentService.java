package com.gradecalculator.service;

import com.gradecalculator.dto.EnrollmentRequest;
import com.gradecalculator.dto.EnrollmentResponse;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Student;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<EnrollmentResponse> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudentIdAndSemesterId(Long studentId, Long semesterId) {
        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findBySemesterId(Long semesterId) {
        return enrollmentRepository.findBySemesterId(semesterId).stream()
                .map(this::toEnrollmentResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
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
    public EnrollmentResponse update(Long id, EnrollmentRequest request) {
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

    public EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();
        return new EnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                student.getName(),
                student.getStudentId(),
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredits(),
                course.getSemester().getId(),
                course.getSemester().getSemesterNumber(),
                enrollment.getGrade().getGrade(),
                enrollment.getGrade().getGradePoints(),
                enrollment.getCreditPoints()
        );
    }
}
