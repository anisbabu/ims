package com.ims.transport.dto;

import com.ims.transport.AssignmentStatus;
import com.ims.transport.TransportAssignment;
import com.ims.transport.TransportRoute;
import com.ims.transport.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TransportDtos {

    private TransportDtos() {
    }

    // ---- Vehicle ----
    public record CreateVehicle(
            @NotBlank String regNo, String model, int capacity, String driverName, String driverPhone) {
    }

    public record UpdateVehicle(
            String regNo, String model, Integer capacity, String driverName, String driverPhone) {
    }

    public record VehicleResponse(
            UUID id, String regNo, String model, int capacity, String driverName, String driverPhone) {
        public static VehicleResponse from(Vehicle v) {
            return new VehicleResponse(v.getId(), v.getRegNo(), v.getModel(), v.getCapacity(),
                    v.getDriverName(), v.getDriverPhone());
        }
    }

    // ---- Route ----
    public record CreateRoute(
            @NotBlank String name, String stops, BigDecimal fare, UUID vehicleId) {
    }

    public record UpdateRoute(String name, String stops, BigDecimal fare, UUID vehicleId) {
    }

    public record RouteResponse(
            UUID id, String name, String stops, BigDecimal fare, UUID vehicleId) {
        public static RouteResponse from(TransportRoute r) {
            return new RouteResponse(r.getId(), r.getName(), r.getStops(), r.getFare(), r.getVehicleId());
        }
    }

    // ---- Assignment ----
    public record Assign(
            @NotNull UUID studentId, @NotNull UUID routeId, String stopName) {
    }

    public record AssignmentResponse(
            UUID id, UUID studentId, UUID routeId, String stopName,
            LocalDate assignedDate, LocalDate endDate, AssignmentStatus status) {
        public static AssignmentResponse from(TransportAssignment a) {
            return new AssignmentResponse(a.getId(), a.getStudentId(), a.getRouteId(), a.getStopName(),
                    a.getAssignedDate(), a.getEndDate(), a.getStatus());
        }
    }
}
