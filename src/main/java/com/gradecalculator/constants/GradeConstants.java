package com.gradecalculator.constants;

/**
 * Centralized constants for grade calculations and thresholds.
 * <p>
 * All magic numbers related to grading are extracted here for maintainability.
 */
public final class GradeConstants {

    private GradeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MARK RANGES (%)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Marks range: 90-100 */
    public static final int MARKS_OUTSTANDING_MIN = 90;
    
    /** Marks range: 80-89 */
    public static final int MARKS_EXCELLENT_MIN = 80;
    
    /** Marks range: 70-79 */
    public static final int MARKS_VERY_GOOD_MIN = 70;
    
    /** Marks range: 60-69 */
    public static final int MARKS_GOOD_MIN = 60;
    
    /** Marks range: 55-59 */
    public static final int MARKS_ABOVE_AVG_MIN = 55;
    
    /** Marks range: 50-54 */
    public static final int MARKS_AVERAGE_MIN = 50;
    
    /** Marks range: 40-49 */
    public static final int MARKS_PASS_MIN = 40;
    
    /** Marks range: 0-39 (Fail) */
    public static final int MARKS_FAIL_MAX = 39;

    // ═══════════════════════════════════════════════════════════════════════════
    // GRADE POINTS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static final int POINTS_OUTSTANDING = 10;
    public static final int POINTS_EXCELLENT = 9;
    public static final int POINTS_VERY_GOOD = 8;
    public static final int POINTS_GOOD = 7;
    public static final int POINTS_ABOVE_AVG = 6;
    public static final int POINTS_AVERAGE = 5;
    public static final int POINTS_PASS = 4;
    public static final int POINTS_FAIL = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    // CIE (Continuous Internal Evaluation) PASS THRESHOLDS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Theory CIE minimum passing marks (out of 50) */
    public static final int CIE_THEORY_PASS_MIN = 20;
    
    /** Lab CIE minimum passing marks (out of 50) */
    public static final int CIE_LAB_PASS_MIN = 25;
    
    /** Theory component minimum for INTEGRATED courses */
    public static final int CIE_INTEGRATED_THEORY_MIN = 12;
    
    /** Lab component minimum for INTEGRATED courses */
    public static final int CIE_INTEGRATED_LAB_MIN = 8;

    // ═══════════════════════════════════════════════════════════════════════════
    // SEE (Semester End Examination) PASS THRESHOLDS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** SEE minimum passing marks (out of 50) */
    public static final int SEE_PASS_MIN = 18;
    
    /** SEE maximum marks (after scaling) */
    public static final int SEE_MAX = 50;

    // ═══════════════════════════════════════════════════════════════════════════
    // OVERALL PASS THRESHOLDS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Minimum total marks to pass any course */
    public static final int TOTAL_MARKS_PASS_MIN = 40;
    
    /** Maximum possible marks (CIE + SEE) */
    public static final int TOTAL_MARKS_MAX = 100;

    // ═══════════════════════════════════════════════════════════════════════════
    // CIE COMPONENT WEIGHTS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Average of Test1 and Test2 scaled to this value */
    public static final double TEST_AVERAGE_SCALE = 0.3;
    
    /** Theory CIE scaled to this percentage of total */
    public static final double CIE_THEORY_WEIGHT = 0.6;
    
    /** Lab CIE scaled to this percentage of total */
    public static final double CIE_LAB_WEIGHT = 0.4;
    
    /** Theory contribution to integrated CIE */
    public static final double INTEGRATED_THEORY_CIE_PORTION = 0.6;
    
    /** Lab contribution to integrated CIE */
    public static final double INTEGRATED_LAB_CIE_PORTION = 0.4;

    // ═══════════════════════════════════════════════════════════════════════════
    // DECIMAL PRECISION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Number of decimal places for SGPA/CGPA rounding */
    public static final int GRADE_DECIMAL_PRECISION = 2;
    
    /** Default credits for a course */
    public static final int DEFAULT_CREDITS = 4;
}
