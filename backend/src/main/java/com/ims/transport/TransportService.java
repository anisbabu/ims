package com.ims.transport;

import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import com.ims.transport.dto.TransportDtos.Assign;
import com.ims.transport.dto.TransportDtos.AssignmentResponse;
import com.ims.transport.dto.TransportDtos.CreateRoute;
import com.ims.transport.dto.TransportDtos.CreateVehicle;
import com.ims.transport.dto.TransportDtos.RouteResponse;
import com.ims.transport.dto.TransportDtos.UpdateRoute;
import com.ims.transport.dto.TransportDtos.UpdateVehicle;
import com.ims.transport.dto.TransportDtos.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TransportService {

    private final VehicleRepository vehicleRepository;
    private final TransportRouteRepository routeRepository;
    private final TransportAssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    public TransportService(VehicleRepository vehicleRepository,
                            TransportRouteRepository routeRepository,
                            TransportAssignmentRepository assignmentRepository,
                            StudentRepository studentRepository) {
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
    }

    // ---- Vehicle ----

    @Transactional
    public VehicleResponse createVehicle(CreateVehicle req) {
        Vehicle v = new Vehicle();
        v.setRegNo(req.regNo());
        v.setModel(req.model());
        v.setCapacity(req.capacity());
        v.setDriverName(req.driverName());
        v.setDriverPhone(req.driverPhone());
        return VehicleResponse.from(vehicleRepository.save(v));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles() {
        return vehicleRepository.findAll().stream()
                .sorted(Comparator.comparing(Vehicle::getRegNo))
                .map(VehicleResponse::from).toList();
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, UpdateVehicle req) {
        Vehicle v = requireVehicle(id);
        if (req.regNo() != null) v.setRegNo(req.regNo());
        if (req.model() != null) v.setModel(req.model());
        if (req.capacity() != null) v.setCapacity(req.capacity());
        if (req.driverName() != null) v.setDriverName(req.driverName());
        if (req.driverPhone() != null) v.setDriverPhone(req.driverPhone());
        return VehicleResponse.from(v);
    }

    @Transactional
    public void deleteVehicle(UUID id) {
        vehicleRepository.delete(requireVehicle(id));
    }

    // ---- Route ----

    @Transactional
    public RouteResponse createRoute(CreateRoute req) {
        if (req.vehicleId() != null) requireVehicle(req.vehicleId());
        TransportRoute r = new TransportRoute();
        r.setName(req.name());
        r.setStops(req.stops());
        r.setFare(req.fare());
        r.setVehicleId(req.vehicleId());
        return RouteResponse.from(routeRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> listRoutes() {
        return routeRepository.findAll().stream()
                .sorted(Comparator.comparing(TransportRoute::getName))
                .map(RouteResponse::from).toList();
    }

    @Transactional
    public RouteResponse updateRoute(UUID id, UpdateRoute req) {
        TransportRoute r = requireRoute(id);
        if (req.name() != null) r.setName(req.name());
        if (req.stops() != null) r.setStops(req.stops());
        if (req.fare() != null) r.setFare(req.fare());
        if (req.vehicleId() != null) {
            requireVehicle(req.vehicleId());
            r.setVehicleId(req.vehicleId());
        }
        return RouteResponse.from(r);
    }

    @Transactional
    public void deleteRoute(UUID id) {
        routeRepository.delete(requireRoute(id));
    }

    // ---- Assignment ----

    @Transactional
    public AssignmentResponse assign(Assign req) {
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        requireRoute(req.routeId());
        TransportAssignment a = new TransportAssignment();
        a.setStudentId(req.studentId());
        a.setRouteId(req.routeId());
        a.setStopName(req.stopName());
        a.setAssignedDate(LocalDate.now());
        a.setStatus(AssignmentStatus.ACTIVE);
        return AssignmentResponse.from(assignmentRepository.save(a));
    }

    @Transactional
    public AssignmentResponse end(UUID id) {
        TransportAssignment a = assignmentRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        if (a.getStatus() == AssignmentStatus.ENDED) {
            throw new BadRequestException("Already ended");
        }
        a.setStatus(AssignmentStatus.ENDED);
        a.setEndDate(LocalDate.now());
        return AssignmentResponse.from(a);
    }

    @Transactional(readOnly = true)
    public PageResponse<AssignmentResponse> listAssignments(AssignmentStatus status, UUID routeId, Pageable pageable) {
        Page<TransportAssignment> page;
        if (routeId != null) {
            page = assignmentRepository.findByRouteId(routeId, pageable);
        } else if (status != null) {
            page = assignmentRepository.findByStatus(status, pageable);
        } else {
            page = assignmentRepository.findAll(pageable);
        }
        return PageResponse.from(page, AssignmentResponse::from);
    }

    private Vehicle requireVehicle(UUID id) {
        return vehicleRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    }

    private TransportRoute requireRoute(UUID id) {
        return routeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Route not found"));
    }
}
