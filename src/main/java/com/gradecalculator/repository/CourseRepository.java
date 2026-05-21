package com.gradecalculator.repository;

import com.gradecalculator.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE c.courseCode = :courseCode AND c.semester.id = :semesterId")
    Optional<Course> findByCourseCodeAndSemesterId(@Param("courseCode") String courseCode, @Param("semesterId") Long semesterId);

    List<Course> findBySemesterId(Long semesterId);
}