package com.gradecalculator.repository;

import com.gradecalculator.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository layer tests using @DataJpaTest for accurate JPA testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Student student;
    private Course course;
    private Semester semester;

    @BeforeEach
    void setUp() {
        semester = new Semester(1);
        entityManager.persist(semester);

        student = new Student("Test Student", "STU2026001");
        entityManager.persist(student);

        course = new Course("TEST101", "Test Course", 4);
        course.setSemester(semester);
        entityManager.persist(course);

        entityManager.flush();
    }

    @Test
    void findByStudentIdReturnsCorrectEnrollments() {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setGrade(LetterGrade.A);
        entityManager.persist(enrollment);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findByStudentId(student.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStudent().getId()).isEqualTo(student.getId());
    }

    @Test
    void findByStudentIdReturnsEmptyListForNoEnrollments() {
        List<Enrollment> results = enrollmentRepository.findByStudentId(student.getId());

        assertThat(results).isEmpty();
    }

    @Test
    void findByCourseIdReturnsCorrectEnrollments() {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setGrade(LetterGrade.B_PLUS);
        entityManager.persist(enrollment);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findByCourseId(course.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void findByStudentIdAndSemesterIdReturnsCorrectEnrollments() {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setGrade(LetterGrade.O);
        entityManager.persist(enrollment);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findByStudentIdAndSemesterId(
                student.getId(), semester.getId());

        assertThat(results).hasSize(1);
    }

    @Test
    void findByStudentIdAndSemesterIdIgnoresOtherSemesters() {
        // Create another semester
        Semester semester2 = new Semester(2);
        entityManager.persist(semester2);

        Course course2 = new Course("TEST201", "Test Course 2", 4);
        course2.setSemester(semester2);
        entityManager.persist(course2);

        Enrollment enrollment1 = new Enrollment(student, course);
        enrollment1.setGrade(LetterGrade.A);
        entityManager.persist(enrollment1);

        Enrollment enrollment2 = new Enrollment(student, course2);
        enrollment2.setGrade(LetterGrade.B);
        entityManager.persist(enrollment2);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findByStudentIdAndSemesterId(
                student.getId(), semester.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGrade()).isEqualTo(LetterGrade.A);
    }

    @Test
    void existsByStudentIdAndCourseIdReturnsTrueWhenExists() {
        Enrollment enrollment = new Enrollment(student, course);
        entityManager.persist(enrollment);
        entityManager.flush();

        boolean exists = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByStudentIdAndCourseIdReturnsFalseWhenNotExists() {
        boolean exists = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void findByStudentIdAndCourseIdReturnsCorrectEnrollment() {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setGrade(LetterGrade.C);
        entityManager.persist(enrollment);
        entityManager.flush();

        Optional<Enrollment> result = enrollmentRepository.findByStudentIdAndCourseId(
                student.getId(), course.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getGrade()).isEqualTo(LetterGrade.C);
    }

    @Test
    void findByStudentIdAndCourseIdReturnsEmptyForNonExistent() {
        Optional<Enrollment> result = enrollmentRepository.findByStudentIdAndCourseId(
                9999L, 9999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByStudentIdExcludingFExcludesFailedStudents() {
        Enrollment passEnrollment = new Enrollment(student, course);
        passEnrollment.setGrade(LetterGrade.A);
        entityManager.persist(passEnrollment);

        // Create another course that the student failed
        Course failedCourse = new Course("TEST102", "Failed Course", 4);
        failedCourse.setSemester(semester);
        entityManager.persist(failedCourse);

        Enrollment failEnrollment = new Enrollment(student, failedCourse);
        failEnrollment.setGrade(LetterGrade.F);
        entityManager.persist(failEnrollment);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findByStudentIdExcludingF(student.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGrade()).isEqualTo(LetterGrade.A);
    }

    @Test
    void findBySemesterIdReturnsAllEnrollmentsForSemester() {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setGrade(LetterGrade.B_PLUS);
        entityManager.persist(enrollment);
        entityManager.flush();

        List<Enrollment> results = enrollmentRepository.findBySemesterId(semester.getId());

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(e -> e.getCourse().getSemester().getId().equals(semester.getId()));
    }
}
