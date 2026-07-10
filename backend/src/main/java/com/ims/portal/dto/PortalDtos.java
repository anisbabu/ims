package com.ims.portal.dto;

import com.ims.admission.AdmissionStatus;
import com.ims.attendance.dto.AttendanceDtos.AttendanceSummary;
import com.ims.fee.dto.FeeDtos.FeeSummary;
import com.ims.people.dto.PeopleDtos.GuardianResponse;
import com.ims.people.dto.PeopleDtos.StudentResponse;
import com.ims.people.dto.PeopleDtos.TeacherResponse;
import com.ims.report.dto.ReportDtos.StudentMarksReport;
import com.ims.routine.dto.RoutineDtos.SlotResponse;

import java.util.List;
import java.util.UUID;

public final class PortalDtos {

    private PortalDtos() {
    }

    /** The student's current enrollment, with names resolved for display. */
    public record AdmissionInfo(
            UUID admissionId, UUID academicYearId, String academicYear,
            UUID gradeId, String grade, UUID sectionId, String section,
            AdmissionStatus status) {
    }

    public record StudentPortal(
            StudentResponse student, AdmissionInfo admission, AttendanceSummary attendance,
            FeeSummary fees, StudentMarksReport marks, List<SlotResponse> routine) {
    }

    public record ChildSummary(
            UUID studentId, String fullName, AdmissionInfo admission,
            AttendanceSummary attendance, FeeSummary fees) {
    }

    public record GuardianPortal(GuardianResponse guardian, List<ChildSummary> children) {
    }

    public record SectionInfo(UUID id, String name, String grade) {
    }

    public record TeacherPortal(
            TeacherResponse teacher, List<SectionInfo> classTeacherOf, List<SlotResponse> routine) {
    }
}
