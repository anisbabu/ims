package com.ims.fee;

import com.ims.auth.Role;
import com.ims.auth.SecurityUser;
import com.ims.auth.User;
import com.ims.auth.UserRepository;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.fee.dto.FeeDtos.RecordPayment;
import com.ims.fee.dto.FeeStructureDtos.CheckoutRequest;
import com.ims.fee.dto.FeeStructureDtos.OnlinePaymentResponse;
import com.ims.people.StudentGuardianRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class OnlinePaymentService {

    private final OnlinePaymentRepository onlinePaymentRepository;
    private final FeeRepository feeRepository;
    private final FeeService feeService;
    private final UserRepository userRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final PaymentGatewayProvider gateway;

    public OnlinePaymentService(OnlinePaymentRepository onlinePaymentRepository,
                                FeeRepository feeRepository,
                                FeeService feeService,
                                UserRepository userRepository,
                                StudentGuardianRepository studentGuardianRepository,
                                PaymentGatewayProvider gateway) {
        this.onlinePaymentRepository = onlinePaymentRepository;
        this.feeRepository = feeRepository;
        this.feeService = feeService;
        this.userRepository = userRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.gateway = gateway;
    }

    @Transactional
    public OnlinePaymentResponse checkout(SecurityUser me, CheckoutRequest req) {
        Fee fee = feeRepository.findById(req.feeId()).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Fee not found"));
        requirePayer(me, fee.getStudentId());
        if (fee.getStatus() == FeeStatus.WAIVED || fee.getStatus() == FeeStatus.PAID) {
            throw new BadRequestException("Fee has nothing outstanding");
        }
        BigDecimal due = fee.getAmount().subtract(fee.getPaidAmount());
        BigDecimal amount = req.amount() != null ? req.amount() : due;
        if (amount.compareTo(due) > 0) {
            throw new BadRequestException("Amount exceeds outstanding due");
        }
        OnlinePayment p = new OnlinePayment();
        p.setFeeId(fee.getId());
        p.setStudentId(fee.getStudentId());
        p.setUserId(me.getId());
        p.setAmount(amount);
        p.setProvider(gateway.name());
        p = onlinePaymentRepository.save(p);
        return toResponse(p, fee);
    }

    @Transactional(readOnly = true)
    public OnlinePaymentResponse get(UUID id, SecurityUser me) {
        OnlinePayment p = requireOwn(id, me);
        return toResponse(p, requireFee(p.getFeeId()));
    }

    /** Mock-provider completion; with a real gateway this work moves to the webhook handler. */
    @Transactional
    public OnlinePaymentResponse confirm(UUID id, SecurityUser me) {
        OnlinePayment p = requireOwn(id, me);
        if (p.getStatus() != OnlinePaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + p.getStatus().name().toLowerCase());
        }
        String reference = "OP-" + p.getId().toString().substring(0, 8).toUpperCase();
        feeService.recordPayment(p.getFeeId(),
                new RecordPayment(p.getAmount(), PaymentMethod.ONLINE, reference, LocalDate.now()));
        p.setStatus(OnlinePaymentStatus.SUCCESS);
        p.setReference(reference);
        p.setPaidAt(Instant.now());
        return toResponse(p, requireFee(p.getFeeId()));
    }

    @Transactional
    public OnlinePaymentResponse cancel(UUID id, SecurityUser me) {
        OnlinePayment p = requireOwn(id, me);
        if (p.getStatus() != OnlinePaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + p.getStatus().name().toLowerCase());
        }
        p.setStatus(OnlinePaymentStatus.CANCELLED);
        return toResponse(p, requireFee(p.getFeeId()));
    }

    /** Caller must be the student the fee belongs to, or a guardian linked to them. */
    private void requirePayer(SecurityUser me, UUID studentId) {
        User user = userRepository.findById(me.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        UUID profileId = user.getProfileId();
        boolean allowed = profileId != null
                && ((me.getRole() == Role.STUDENT && studentId.equals(profileId))
                || (me.getRole() == Role.GUARDIAN
                        && studentGuardianRepository.existsByStudentIdAndGuardianId(studentId, profileId)));
        if (!allowed) {
            throw new NotFoundException("Fee not found");
        }
    }

    private OnlinePayment requireOwn(UUID id, SecurityUser me) {
        return onlinePaymentRepository.findById(id).map(TenantGuard::owned)
                .filter(p -> p.getUserId().equals(me.getId()))
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    private Fee requireFee(UUID feeId) {
        return feeRepository.findById(feeId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Fee not found"));
    }

    private OnlinePaymentResponse toResponse(OnlinePayment p, Fee fee) {
        String checkoutUrl = p.getStatus() == OnlinePaymentStatus.PENDING ? gateway.checkoutUrl(p) : null;
        return OnlinePaymentResponse.from(p, fee.getTitle(), checkoutUrl);
    }
}
