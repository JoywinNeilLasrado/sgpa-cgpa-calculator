package com.gradecalculator.repository;

import com.gradecalculator.model.Enrollment;
import com.gradecalculator.model.LetterGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.semester.id = :semesterId")
    List<Enrollment> findByStudentIdAndSemesterId(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.grade != 'F'")
    List<Enrollment> findByStudentIdExcludingF(@Param("studentId") Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
