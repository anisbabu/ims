package com.ims.hostel;

import com.ims.common.PageResponse;
import com.ims.hostel.dto.HostelDtos.Allocate;
import com.ims.hostel.dto.HostelDtos.AllocationResponse;
import com.ims.hostel.dto.HostelDtos.CreateHostel;
import com.ims.hostel.dto.HostelDtos.CreateRoom;
import com.ims.hostel.dto.HostelDtos.HostelResponse;
import com.ims.hostel.dto.HostelDtos.RoomResponse;
import com.ims.hostel.dto.HostelDtos.UpdateHostel;
import com.ims.hostel.dto.HostelDtos.UpdateRoom;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hostel")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    // Hostels
    @PostMapping("/hostels")
    @ResponseStatus(HttpStatus.CREATED)
    public HostelResponse createHostel(@Valid @RequestBody CreateHostel req) {
        return hostelService.createHostel(req);
    }

    @GetMapping("/hostels")
    public List<HostelResponse> listHostels() {
        return hostelService.listHostels();
    }

    @PutMapping("/hostels/{id}")
    public HostelResponse updateHostel(@PathVariable UUID id, @Valid @RequestBody UpdateHostel req) {
        return hostelService.updateHostel(id, req);
    }

    @DeleteMapping("/hostels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHostel(@PathVariable UUID id) {
        hostelService.deleteHostel(id);
    }

    // Rooms
    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody CreateRoom req) {
        return hostelService.createRoom(req);
    }

    @GetMapping("/rooms")
    public List<RoomResponse> listRooms(@RequestParam(required = false) UUID hostelId) {
        return hostelService.listRooms(hostelId);
    }

    @PutMapping("/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable UUID id, @Valid @RequestBody UpdateRoom req) {
        return hostelService.updateRoom(id, req);
    }

    @DeleteMapping("/rooms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable UUID id) {
        hostelService.deleteRoom(id);
    }

    // Allocations
    @PostMapping("/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public AllocationResponse allocate(@Valid @RequestBody Allocate req) {
        return hostelService.allocate(req);
    }

    @GetMapping("/allocations")
    public PageResponse<AllocationResponse> listAllocations(@RequestParam(required = false) AllocationStatus status,
                                                            @RequestParam(required = false) UUID hostelId,
                                                            Pageable pageable) {
        return hostelService.listAllocations(status, hostelId, pageable);
    }

    @PatchMapping("/allocations/{id}/vacate")
    public AllocationResponse vacate(@PathVariable UUID id) {
        return hostelService.vacate(id);
    }
}
