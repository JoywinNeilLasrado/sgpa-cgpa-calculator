package com.gradecalculator.service;

import com.gradecalculator.model.*;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptPdfServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    private TranscriptPdfService service;

    @BeforeEach
    void setUp() {
        service = new TranscriptPdfService(enrollmentRepository, studentRepository);
    }

    @Test
    void generateTranscriptThrowsWhenStudentNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateTranscript(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student not found");
    }

    @Test
    void generateTranscriptReturnsPdfByteArray() throws Exception {
        Long studentId = 1L;
        Student student = new Student("Jane Doe", "CS2024002");
        student.setId(studentId);

        Course course = new Course("CS101", "Programming in Java", 4);
        Enrollment enrollment = new Enrollment(student, course, LetterGrade.A_PLUS);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(enrollment));

        byte[] pdfBytes = service.generateTranscript(studentId);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        
        // PDF magic number check (starts with %PDF)
        assertThat(new String(Arrays.copyOfRange(pdfBytes, 0, 4))).isEqualTo("%PDF");

        verify(studentRepository).findById(studentId);
        verify(enrollmentRepository).findByStudentId(studentId);
    }
}
