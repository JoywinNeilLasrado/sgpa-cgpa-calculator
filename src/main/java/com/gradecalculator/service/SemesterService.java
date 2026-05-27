package com.gradecalculator.service;

import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.exception.NotFoundException;
import com.gradecalculator.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public SemesterService(
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Semester> findAll() {
        return semesterRepository.findAll();
    }

    public Optional<Semester> findById(Long id) {
        return semesterRepository.findById(id);
    }

    public Semester create(Integer semesterNumber) {
        validateSemesterNumber(semesterNumber);
        return semesterRepository.save(new Semester(semesterNumber));
    }

    public Semester update(Long id, Integer semesterNumber) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        validateSemesterNumber(semesterNumber);
        semester.setSemesterNumber(semesterNumber);
        return semesterRepository.save(semester);
    }

    @Transactional
    public void delete(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        List<Course> courses = courseRepository.findBySemesterId(id);
        for (Course course : courses) {
            enrollmentRepository.deleteAll(enrollmentRepository.findByCourseId(course.getId()));
        }
        courseRepository.deleteAll(courses);
        semesterRepository.delete(semester);
    }

    private void validateSemesterNumber(Integer semesterNumber) {
        if (semesterNumber == null || semesterNumber < 1) {
            throw new ValidationException("Semester number must be greater than zero");
        }
    }
}
