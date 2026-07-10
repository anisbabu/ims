package com.ims.hostel;

import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.hostel.dto.HostelDtos.Allocate;
import com.ims.hostel.dto.HostelDtos.AllocationResponse;
import com.ims.hostel.dto.HostelDtos.CreateHostel;
import com.ims.hostel.dto.HostelDtos.CreateRoom;
import com.ims.hostel.dto.HostelDtos.HostelResponse;
import com.ims.hostel.dto.HostelDtos.RoomResponse;
import com.ims.hostel.dto.HostelDtos.UpdateHostel;
import com.ims.hostel.dto.HostelDtos.UpdateRoom;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HostelService {

    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;
    private final HostelAllocationRepository allocationRepository;
    private final StudentRepository studentRepository;

    public HostelService(HostelRepository hostelRepository,
                         RoomRepository roomRepository,
                         HostelAllocationRepository allocationRepository,
                         StudentRepository studentRepository) {
        this.hostelRepository = hostelRepository;
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
        this.studentRepository = studentRepository;
    }

    // ---- Hostel ----

    @Transactional
    public HostelResponse createHostel(CreateHostel req) {
        Hostel h = new Hostel();
        h.setName(req.name());
        h.setType(req.type());
        h.setAddress(req.address());
        h.setWardenId(req.wardenId());
        return HostelResponse.from(hostelRepository.save(h));
    }

    @Transactional(readOnly = true)
    public List<HostelResponse> listHostels() {
        return hostelRepository.findAll().stream()
                .sorted(Comparator.comparing(Hostel::getName))
                .map(HostelResponse::from).toList();
    }

    @Transactional
    public HostelResponse updateHostel(UUID id, UpdateHostel req) {
        Hostel h = requireHostel(id);
        if (req.name() != null) h.setName(req.name());
        if (req.type() != null) h.setType(req.type());
        if (req.address() != null) h.setAddress(req.address());
        if (req.wardenId() != null) h.setWardenId(req.wardenId());
        return HostelResponse.from(h);
    }

    @Transactional
    public void deleteHostel(UUID id) {
        hostelRepository.delete(requireHostel(id));
    }

    // ---- Room ----

    @Transactional
    public RoomResponse createRoom(CreateRoom req) {
        requireHostel(req.hostelId());
        Room r = new Room();
        r.setHostelId(req.hostelId());
        r.setRoomNo(req.roomNo());
        r.setCapacity(req.capacity());
        r.setOccupied(0);
        return RoomResponse.from(roomRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(UUID hostelId) {
        List<Room> rooms = hostelId != null ? roomRepository.findByHostelId(hostelId) : roomRepository.findAll();
        return rooms.stream().map(RoomResponse::from).toList();
    }

    @Transactional
    public RoomResponse updateRoom(UUID id, UpdateRoom req) {
        Room r = requireRoom(id);
        if (req.roomNo() != null) r.setRoomNo(req.roomNo());
        if (req.capacity() != null) r.setCapacity(req.capacity());
        return RoomResponse.from(r);
    }

    @Transactional
    public void deleteRoom(UUID id) {
        roomRepository.delete(requireRoom(id));
    }

    // ---- Allocation ----

    @Transactional
    public AllocationResponse allocate(Allocate req) {
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        Room room = requireRoom(req.roomId());
        if (room.getOccupied() >= room.getCapacity()) {
            throw new BadRequestException("Room is full");
        }
        room.setOccupied(room.getOccupied() + 1);

        HostelAllocation a = new HostelAllocation();
        a.setStudentId(req.studentId());
        a.setHostelId(room.getHostelId());
        a.setRoomId(room.getId());
        a.setAllocatedDate(LocalDate.now());
        a.setStatus(AllocationStatus.ALLOCATED);
        return AllocationResponse.from(allocationRepository.save(a));
    }

    @Transactional
    public AllocationResponse vacate(UUID allocationId) {
        HostelAllocation a = allocationRepository.findById(allocationId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Allocation not found"));
        if (a.getStatus() == AllocationStatus.VACATED) {
            throw new BadRequestException("Already vacated");
        }
        a.setStatus(AllocationStatus.VACATED);
        a.setVacatedDate(LocalDate.now());
        Room room = requireRoom(a.getRoomId());
        room.setOccupied(Math.max(0, room.getOccupied() - 1));
        return AllocationResponse.from(a);
    }

    @Transactional(readOnly = true)
    public PageResponse<AllocationResponse> listAllocations(AllocationStatus status, UUID hostelId, Pageable pageable) {
        Page<HostelAllocation> page;
        if (hostelId != null) {
            page = allocationRepository.findByHostelId(hostelId, pageable);
        } else if (status != null) {
            page = allocationRepository.findByStatus(status, pageable);
        } else {
            page = allocationRepository.findAll(pageable);
        }
        return PageResponse.from(page, AllocationResponse::from);
    }

    private Hostel requireHostel(UUID id) {
        return hostelRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Hostel not found"));
    }

    private Room requireRoom(UUID id) {
        return roomRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }
}
