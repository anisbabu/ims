package com.ims.routine;

import com.ims.routine.dto.RoutineDtos.CreateSlot;
import com.ims.routine.dto.RoutineDtos.SlotResponse;
import com.ims.routine.dto.RoutineDtos.UpdateSlot;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public SlotResponse create(@Valid @RequestBody CreateSlot req) {
        return routineService.create(req);
    }

    @GetMapping
    public List<SlotResponse> list(@RequestParam(required = false) RoutineKind kind,
                                   @RequestParam(required = false) UUID sectionId,
                                   @RequestParam(required = false) UUID examId) {
        return routineService.list(kind, sectionId, examId);
    }

    @PutMapping("/{id}")
    @PreAuthorize(STAFF)
    public SlotResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSlot req) {
        return routineService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(STAFF)
    public void delete(@PathVariable UUID id) {
        routineService.delete(id);
    }
}
