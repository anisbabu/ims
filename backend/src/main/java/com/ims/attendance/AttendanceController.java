package com.ims.attendance;

import com.ims.attendance.dto.AttendanceDtos.AttendanceResponse;
import com.ims.attendance.dto.AttendanceDtos.AttendanceSummary;
import com.ims.attendance.dto.AttendanceDtos.MarkAttendance;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public List<AttendanceResponse> mark(@Valid @RequestBody MarkAttendance req) {
        return attendanceService.mark(req);
    }

    @GetMapping
    public List<AttendanceResponse> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID sectionId) {
        return attendanceService.listByDate(date, sectionId);
    }

    @GetMapping("/summary")
    public AttendanceSummary summary(
            @RequestParam UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.summary(studentId, from, to);
    }
}
