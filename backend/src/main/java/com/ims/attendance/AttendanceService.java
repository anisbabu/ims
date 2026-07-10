package com.ims.attendance;

import com.ims.attendance.dto.AttendanceDtos.AttendanceResponse;
import com.ims.attendance.dto.AttendanceDtos.AttendanceSummary;
import com.ims.attendance.dto.AttendanceDtos.MarkAttendance;
import com.ims.attendance.dto.AttendanceDtos.MarkEntry;
import com.ims.common.BadRequestException;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    /** Upsert attendance for each student on the given date. */
    @Transactional
    public List<AttendanceResponse> mark(MarkAttendance req) {
        List<AttendanceResponse> result = new ArrayList<>();
        for (MarkEntry entry : req.entries()) {
            studentRepository.findById(entry.studentId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Student not found: " + entry.studentId()));
            Attendance a = attendanceRepository
                    .findByStudentIdAndDate(entry.studentId(), req.date())
                    .orElseGet(Attendance::new);
            a.setStudentId(entry.studentId());
            a.setDate(req.date());
            a.setSectionId(req.sectionId());
            a.setStatus(entry.status());
            a.setRemarks(entry.remarks());
            result.add(AttendanceResponse.from(attendanceRepository.save(a)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> listByDate(LocalDate date, UUID sectionId) {
        List<Attendance> rows = (sectionId != null)
                ? attendanceRepository.findByDateAndSectionId(date, sectionId)
                : attendanceRepository.findByDate(date);
        return rows.stream().map(AttendanceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSummary summary(UUID studentId, LocalDate from, LocalDate to) {
        studentRepository.findById(studentId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        List<Attendance> rows = attendanceRepository.findByStudentIdAndDateBetween(studentId, from, to);
        Map<AttendanceStatus, Long> counts = new EnumMap<>(AttendanceStatus.class);
        for (AttendanceStatus s : AttendanceStatus.values()) counts.put(s, 0L);
        for (Attendance a : rows) counts.merge(a.getStatus(), 1L, Long::sum);
        int total = rows.size();
        long present = counts.get(AttendanceStatus.PRESENT) + counts.get(AttendanceStatus.LATE);
        double pct = total == 0 ? 0 : Math.round((present * 10000.0) / total) / 100.0;
        return new AttendanceSummary(studentId, from, to, total, counts, pct);
    }
}
