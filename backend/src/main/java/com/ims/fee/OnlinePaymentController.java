package com.ims.fee;

import com.ims.auth.SecurityUser;
import com.ims.fee.dto.FeeStructureDtos.CheckoutRequest;
import com.ims.fee.dto.FeeStructureDtos.OnlinePaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Self-service online fee payments (students paying own fees, guardians their children's). */
@RestController
@RequestMapping("/api/payments")
public class OnlinePaymentController {

    private final OnlinePaymentService onlinePaymentService;

    public OnlinePaymentController(OnlinePaymentService onlinePaymentService) {
        this.onlinePaymentService = onlinePaymentService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('STUDENT','GUARDIAN')")
    public OnlinePaymentResponse checkout(@AuthenticationPrincipal SecurityUser me,
                                          @Valid @RequestBody CheckoutRequest req) {
        return onlinePaymentService.checkout(me, req);
    }

    @GetMapping("/{id}")
    public OnlinePaymentResponse get(@PathVariable UUID id, @AuthenticationPrincipal SecurityUser me) {
        return onlinePaymentService.get(id, me);
    }

    @PostMapping("/{id}/confirm")
    public OnlinePaymentResponse confirm(@PathVariable UUID id, @AuthenticationPrincipal SecurityUser me) {
        return onlinePaymentService.confirm(id, me);
    }

    @PostMapping("/{id}/cancel")
    public OnlinePaymentResponse cancel(@PathVariable UUID id, @AuthenticationPrincipal SecurityUser me) {
        return onlinePaymentService.cancel(id, me);
    }
}
