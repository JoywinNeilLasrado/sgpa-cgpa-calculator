package com.gradecalculator.model;

/**
 * Enum representing letter grades and their corresponding grade points
 * based on the 10-point grading scale.
 */
public enum LetterGrade {
    O("O", "Outstanding", 10),
    A_PLUS("A+", "Excellent", 9),
    A("A", "Very Good", 8),
    B_PLUS("B+", "Good", 7),
    B("B", "Above Average", 6),
    C("C", "Average", 5),
    P("P", "Pass", 4),
    F("F", "Fail", 0);

    private final String grade;
    private final String performanceLevel;
    private final int gradePoints;

    LetterGrade(String grade, String performanceLevel, int gradePoints) {
        this.grade = grade;
        this.performanceLevel = performanceLevel;
        this.gradePoints = gradePoints;
    }

    public String getGrade() {
        return grade;
    }

    public String getPerformanceLevel() {
        return performanceLevel;
    }

    public int getGradePoints() {
        return gradePoints;
    }

    /**
     * Get the LetterGrade from the grade string representation.
     */
    public static LetterGrade fromGrade(String grade) {
        for (LetterGrade lg : values()) {
            if (lg.grade.equalsIgnoreCase(grade)) {
                return lg;
            }
        }
        throw new IllegalArgumentException("Invalid grade: " + grade);
    }
}