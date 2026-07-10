package com.ims.transport;

import com.ims.common.PageResponse;
import com.ims.transport.dto.TransportDtos.Assign;
import com.ims.transport.dto.TransportDtos.AssignmentResponse;
import com.ims.transport.dto.TransportDtos.CreateRoute;
import com.ims.transport.dto.TransportDtos.CreateVehicle;
import com.ims.transport.dto.TransportDtos.RouteResponse;
import com.ims.transport.dto.TransportDtos.UpdateRoute;
import com.ims.transport.dto.TransportDtos.UpdateVehicle;
import com.ims.transport.dto.TransportDtos.VehicleResponse;
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
@RequestMapping("/api/transport")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    // Vehicles
    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody CreateVehicle req) {
        return transportService.createVehicle(req);
    }

    @GetMapping("/vehicles")
    public List<VehicleResponse> listVehicles() {
        return transportService.listVehicles();
    }

    @PutMapping("/vehicles/{id}")
    public VehicleResponse updateVehicle(@PathVariable UUID id, @Valid @RequestBody UpdateVehicle req) {
        return transportService.updateVehicle(id, req);
    }

    @DeleteMapping("/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable UUID id) {
        transportService.deleteVehicle(id);
    }

    // Routes
    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponse createRoute(@Valid @RequestBody CreateRoute req) {
        return transportService.createRoute(req);
    }

    @GetMapping("/routes")
    public List<RouteResponse> listRoutes() {
        return transportService.listRoutes();
    }

    @PutMapping("/routes/{id}")
    public RouteResponse updateRoute(@PathVariable UUID id, @Valid @RequestBody UpdateRoute req) {
        return transportService.updateRoute(id, req);
    }

    @DeleteMapping("/routes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@PathVariable UUID id) {
        transportService.deleteRoute(id);
    }

    // Assignments
    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse assign(@Valid @RequestBody Assign req) {
        return transportService.assign(req);
    }

    @GetMapping("/assignments")
    public PageResponse<AssignmentResponse> listAssignments(@RequestParam(required = false) AssignmentStatus status,
                                                            @RequestParam(required = false) UUID routeId,
                                                            Pageable pageable) {
        return transportService.listAssignments(status, routeId, pageable);
    }

    @PatchMapping("/assignments/{id}/end")
    public AssignmentResponse end(@PathVariable UUID id) {
        return transportService.end(id);
    }
}
