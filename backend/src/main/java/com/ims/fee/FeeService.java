package com.ims.fee;

import com.ims.accounting.PostingService;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.fee.dto.FeeDtos.CreateFee;
import com.ims.fee.dto.FeeDtos.FeeResponse;
import com.ims.fee.dto.FeeDtos.FeeSummary;
import com.ims.fee.dto.FeeDtos.PaymentResponse;
import com.ims.fee.dto.FeeDtos.RecordPayment;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final PostingService postingService;

    public FeeService(FeeRepository feeRepository,
                      PaymentRepository paymentRepository,
                      StudentRepository studentRepository,
                      PostingService postingService) {
        this.feeRepository = feeRepository;
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.postingService = postingService;
    }

    @Transactional
    public FeeResponse createFee(CreateFee req) {
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        Fee fee = new Fee();
        fee.setStudentId(req.studentId());
        fee.setAcademicYearId(req.academicYearId());
        fee.setTitle(req.title());
        fee.setAmount(req.amount());
        fee.setPaidAmount(BigDecimal.ZERO);
        fee.setDueDate(req.dueDate());
        fee.setStatus(FeeStatus.PENDING);
        return FeeResponse.from(feeRepository.save(fee));
    }

    @Transactional
    public PaymentResponse recordPayment(UUID feeId, RecordPayment req) {
        Fee fee = requireFee(feeId);
        if (fee.getStatus() == FeeStatus.WAIVED) {
            throw new BadRequestException("Fee is waived");
        }
        BigDecimal newPaid = fee.getPaidAmount().add(req.amount());
        if (newPaid.compareTo(fee.getAmount()) > 0) {
            throw new BadRequestException("Payment exceeds outstanding due");
        }
        Payment p = new Payment();
        p.setFeeId(feeId);
        p.setStudentId(fee.getStudentId());
        p.setAmount(req.amount());
        p.setMethod(req.method() != null ? req.method() : PaymentMethod.CASH);
        p.setReference(req.reference());
        p.setPaidOn(req.paidOn() != null ? req.paidOn() : LocalDate.now());
        Payment saved = paymentRepository.save(p);

        fee.setPaidAmount(newPaid);
        fee.setStatus(recompute(fee));

        // Auto-journal: Dr Cash/Bank, Cr Fee Income (cash-basis).
        postingService.postFeePayment(saved.getId(), saved.getStudentId(), saved.getAmount(),
                saved.getMethod().name(), saved.getPaidOn());

        return PaymentResponse.from(saved);
    }

    @Transactional
    public FeeResponse waive(UUID feeId) {
        Fee fee = requireFee(feeId);
        fee.setStatus(FeeStatus.WAIVED);
        return FeeResponse.from(fee);
    }

    @Transactional(readOnly = true)
    public PageResponse<FeeResponse> list(UUID studentId, FeeStatus status, Pageable pageable) {
        var page = studentId != null
                ? feeRepository.findByStudentId(studentId, pageable)
                : status != null
                ? feeRepository.findByStatus(status, pageable)
                : feeRepository.findAll(pageable);
        return PageResponse.from(page, FeeResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> payments(UUID feeId) {
        requireFee(feeId);
        return paymentRepository.findByFeeId(feeId).stream().map(PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FeeSummary summary(UUID studentId) {
        studentRepository.findById(studentId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        BigDecimal billed = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        for (Fee f : fees) {
            if (f.getStatus() == FeeStatus.WAIVED) continue;
            billed = billed.add(f.getAmount());
            paid = paid.add(f.getPaidAmount());
        }
        return new FeeSummary(studentId, billed, paid, billed.subtract(paid),
                fees.stream().map(FeeResponse::from).toList());
    }

    private FeeStatus recompute(Fee fee) {
        int cmp = fee.getPaidAmount().compareTo(fee.getAmount());
        if (cmp >= 0) return FeeStatus.PAID;
        if (fee.getPaidAmount().signum() > 0) return FeeStatus.PARTIAL;
        if (fee.getDueDate() != null && fee.getDueDate().isBefore(LocalDate.now())) return FeeStatus.OVERDUE;
        return FeeStatus.PENDING;
    }

    private Fee requireFee(UUID id) {
        return feeRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Fee not found"));
    }
}
