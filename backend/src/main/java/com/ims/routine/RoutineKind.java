package com.ims.routine;

public enum RoutineKind {
    /** Weekly recurring class period (uses day_of_week). */
    CLASS,
    /** Dated exam sitting (uses slot_date + exam). */
    EXAM
}
