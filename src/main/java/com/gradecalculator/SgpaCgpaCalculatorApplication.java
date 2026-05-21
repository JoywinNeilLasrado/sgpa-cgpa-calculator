package com.gradecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for SGPA CGPA Calculator.
 * This Spring Boot application calculates Semester Grade Point Average (SGPA)
 * and Cumulative Grade Point Average (CGPA) based on the 10-point grading scale.
 */
@SpringBootApplication
public class SgpaCgpaCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgpaCgpaCalculatorApplication.class, args);
    }
}