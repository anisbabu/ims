package com.ims.staff;

import com.ims.common.PageResponse;
import com.ims.staff.dto.EmployeeDtos.CreateEmployee;
import com.ims.staff.dto.EmployeeDtos.EmployeeResponse;
import com.ims.staff.dto.EmployeeDtos.UpdateEmployee;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployee req) {
        return employeeService.create(req);
    }

    @GetMapping
    public PageResponse<EmployeeResponse> list(@RequestParam(required = false) String q, Pageable pageable) {
        return employeeService.list(q, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable UUID id) {
        return employeeService.get(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEmployee req) {
        return employeeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
