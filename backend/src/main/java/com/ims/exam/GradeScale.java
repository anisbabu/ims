package com.ims.exam;

/**
 * Standard percentage-to-letter grading scale. Pass mark is 33%.
 * Used when computing a marksheet.
 */
public final class GradeScale {

    public static final double PASS_PERCENT = 33.0;

    private GradeScale() {
    }

    public static String letter(double percent) {
        if (percent >= 80) return "A+";
        if (percent >= 70) return "A";
        if (percent >= 60) return "A-";
        if (percent >= 50) return "B";
        if (percent >= 40) return "C";
        if (percent >= PASS_PERCENT) return "D";
        return "F";
    }

    public static double gradePoint(double percent) {
        if (percent >= 80) return 5.0;
        if (percent >= 70) return 4.0;
        if (percent >= 60) return 3.5;
        if (percent >= 50) return 3.0;
        if (percent >= 40) return 2.0;
        if (percent >= PASS_PERCENT) return 1.0;
        return 0.0;
    }

    public static boolean isPass(double percent) {
        return percent >= PASS_PERCENT;
    }
}
