package com.ims.fee;

import com.ims.common.PageResponse;
import com.ims.fee.dto.FeeDtos.CreateFee;
import com.ims.fee.dto.FeeDtos.FeeResponse;
import com.ims.fee.dto.FeeDtos.FeeSummary;
import com.ims.fee.dto.FeeDtos.PaymentResponse;
import com.ims.fee.dto.FeeDtos.RecordPayment;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private static final String ADMIN = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')";

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public FeeResponse createFee(@Valid @RequestBody CreateFee req) {
        return feeService.createFee(req);
    }

    @GetMapping
    public PageResponse<FeeResponse> list(@RequestParam(required = false) UUID studentId,
                                          @RequestParam(required = false) FeeStatus status,
                                          Pageable pageable) {
        return feeService.list(studentId, status, pageable);
    }

    @GetMapping("/summary")
    public FeeSummary summary(@RequestParam UUID studentId) {
        return feeService.summary(studentId);
    }

    @PostMapping("/{feeId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public PaymentResponse recordPayment(@PathVariable UUID feeId, @Valid @RequestBody RecordPayment req) {
        return feeService.recordPayment(feeId, req);
    }

    @GetMapping("/{feeId}/payments")
    public List<PaymentResponse> payments(@PathVariable UUID feeId) {
        return feeService.payments(feeId);
    }

    @PatchMapping("/{feeId}/waive")
    @PreAuthorize(ADMIN)
    public FeeResponse waive(@PathVariable UUID feeId) {
        return feeService.waive(feeId);
    }
}
