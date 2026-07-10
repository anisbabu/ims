package com.ims.hostel.dto;

import com.ims.hostel.AllocationStatus;
import com.ims.hostel.Hostel;
import com.ims.hostel.HostelAllocation;
import com.ims.hostel.HostelType;
import com.ims.hostel.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public final class HostelDtos {

    private HostelDtos() {
    }

    // ---- Hostel ----
    public record CreateHostel(
            @NotBlank String name, @NotNull HostelType type, String address, UUID wardenId) {
    }

    public record UpdateHostel(String name, HostelType type, String address, UUID wardenId) {
    }

    public record HostelResponse(UUID id, String name, HostelType type, String address, UUID wardenId) {
        public static HostelResponse from(Hostel h) {
            return new HostelResponse(h.getId(), h.getName(), h.getType(), h.getAddress(), h.getWardenId());
        }
    }

    // ---- Room ----
    public record CreateRoom(
            @NotNull UUID hostelId, @NotBlank String roomNo, @Positive int capacity) {
    }

    public record UpdateRoom(String roomNo, Integer capacity) {
    }

    public record RoomResponse(
            UUID id, UUID hostelId, String roomNo, int capacity, int occupied) {
        public static RoomResponse from(Room r) {
            return new RoomResponse(r.getId(), r.getHostelId(), r.getRoomNo(), r.getCapacity(), r.getOccupied());
        }
    }

    // ---- Allocation ----
    public record Allocate(
            @NotNull UUID studentId, @NotNull UUID roomId) {
    }

    public record AllocationResponse(
            UUID id, UUID studentId, UUID hostelId, UUID roomId,
            LocalDate allocatedDate, LocalDate vacatedDate, AllocationStatus status) {
        public static AllocationResponse from(HostelAllocation a) {
            return new AllocationResponse(a.getId(), a.getStudentId(), a.getHostelId(), a.getRoomId(),
                    a.getAllocatedDate(), a.getVacatedDate(), a.getStatus());
        }
    }
}
