package com.gradecalculator.repository;

import com.gradecalculator.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE e.student.id = :studentId AND c.semester.id = :semesterId")
    List<Enrollment> findByStudentIdAndSemesterId(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE c.semester.id = :semesterId")
    List<Enrollment> findBySemesterId(@Param("semesterId") Long semesterId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE e.student.id = :studentId AND e.grade != 'F'")
    List<Enrollment> findByStudentIdExcludingF(@Param("studentId") Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course c JOIN FETCH c.semester LEFT JOIN FETCH c.faculty WHERE e.student.id = :studentId AND c.id = :courseId")
    java.util.Optional<Enrollment> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
