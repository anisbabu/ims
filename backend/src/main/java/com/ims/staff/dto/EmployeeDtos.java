package com.ims.staff.dto;

import com.ims.people.Gender;
import com.ims.people.PersonStatus;
import com.ims.staff.Employee;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public record CreateEmployee(
            @NotBlank String fullName,
            String designation,
            LocalDate dob,
            Gender gender,
            String phone,
            String email,
            String address,
            LocalDate joinDate,
            String photoUrl) {
    }

    public record UpdateEmployee(
            String fullName,
            String designation,
            LocalDate dob,
            Gender gender,
            String phone,
            String email,
            String address,
            LocalDate joinDate,
            PersonStatus status,
            String photoUrl) {
    }

    public record EmployeeResponse(
            UUID id, String fullName, String designation, LocalDate dob, Gender gender,
            String phone, String email, String address, LocalDate joinDate,
            PersonStatus status, String photoUrl) {
        public static EmployeeResponse from(Employee e) {
            return new EmployeeResponse(e.getId(), e.getFullName(), e.getDesignation(), e.getDob(),
                    e.getGender(), e.getPhone(), e.getEmail(), e.getAddress(), e.getJoinDate(),
                    e.getStatus(), e.getPhotoUrl());
        }
    }
}
