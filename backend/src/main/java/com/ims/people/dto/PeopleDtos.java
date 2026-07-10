package com.ims.people.dto;

import com.ims.people.Designation;
import com.ims.people.Gender;
import com.ims.people.Guardian;
import com.ims.people.PersonStatus;
import com.ims.people.Relation;
import com.ims.people.Student;
import com.ims.people.StudentGuardian;
import com.ims.people.Teacher;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class PeopleDtos {

    private PeopleDtos() {
    }

    // ---- Student ----
    public record CreateStudent(
            @NotBlank String fullName,
            String regNo,
            String rollNo,
            LocalDate dob,
            Gender gender,
            String photoUrl,
            String phone,
            String email,
            String address) {
    }

    public record UpdateStudent(
            String fullName,
            String rollNo,
            LocalDate dob,
            Gender gender,
            String photoUrl,
            String phone,
            String email,
            String address,
            PersonStatus status) {
    }

    public record StudentResponse(
            UUID id, String fullName, String regNo, String rollNo, LocalDate dob,
            Gender gender, String photoUrl, String phone, String email, String address,
            PersonStatus status) {
        public static StudentResponse from(Student s) {
            return new StudentResponse(s.getId(), s.getFullName(), s.getRegNo(), s.getRollNo(),
                    s.getDob(), s.getGender(), s.getPhotoUrl(), s.getPhone(), s.getEmail(),
                    s.getAddress(), s.getStatus());
        }
    }

    // ---- Teacher ----
    public record CreateTeacher(
            @NotBlank String fullName,
            LocalDate dob,
            Gender gender,
            Designation designation,
            String phone,
            String email,
            String address,
            LocalDate joinDate) {
    }

    public record UpdateTeacher(
            String fullName,
            LocalDate dob,
            Gender gender,
            Designation designation,
            String phone,
            String email,
            String address,
            LocalDate joinDate,
            PersonStatus status) {
    }

    public record TeacherResponse(
            UUID id, String fullName, LocalDate dob, Gender gender, Designation designation,
            String phone, String email, String address, LocalDate joinDate, PersonStatus status) {
        public static TeacherResponse from(Teacher t) {
            return new TeacherResponse(t.getId(), t.getFullName(), t.getDob(), t.getGender(),
                    t.getDesignation(), t.getPhone(), t.getEmail(), t.getAddress(),
                    t.getJoinDate(), t.getStatus());
        }
    }

    // ---- Guardian ----
    public record CreateGuardian(
            @NotBlank String fullName,
            String phone,
            String email,
            String occupation,
            String address) {
    }

    public record UpdateGuardian(
            String fullName,
            String phone,
            String email,
            String occupation,
            String address) {
    }

    public record GuardianResponse(
            UUID id, String fullName, String phone, String email, String occupation, String address) {
        public static GuardianResponse from(Guardian g) {
            return new GuardianResponse(g.getId(), g.getFullName(), g.getPhone(), g.getEmail(),
                    g.getOccupation(), g.getAddress());
        }
    }

    // ---- Student-Guardian link ----
    public record LinkGuardian(
            @NotNull UUID guardianId,
            @NotNull Relation relation,
            boolean primary) {
    }

    public record StudentGuardianResponse(
            UUID id, UUID studentId, UUID guardianId, Relation relation, boolean primary) {
        public static StudentGuardianResponse from(StudentGuardian sg) {
            return new StudentGuardianResponse(sg.getId(), sg.getStudentId(), sg.getGuardianId(),
                    sg.getRelation(), sg.isPrimary());
        }
    }
}
