package com.ims.fee.dto;

import com.ims.fee.Fee;
import com.ims.fee.FeeStatus;
import com.ims.fee.Payment;
import com.ims.fee.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class FeeDtos {

    private FeeDtos() {
    }

    public record CreateFee(
            @NotNull UUID studentId,
            UUID academicYearId,
            @NotBlank String title,
            @NotNull @Positive BigDecimal amount,
            LocalDate dueDate) {
    }

    public record RecordPayment(
            @NotNull @Positive BigDecimal amount,
            PaymentMethod method,
            String reference,
            LocalDate paidOn) {
    }

    public record FeeResponse(
            UUID id, UUID studentId, UUID academicYearId, String title,
            BigDecimal amount, BigDecimal paidAmount, BigDecimal dueAmount,
            LocalDate dueDate, FeeStatus status) {
        public static FeeResponse from(Fee f) {
            return new FeeResponse(f.getId(), f.getStudentId(), f.getAcademicYearId(), f.getTitle(),
                    f.getAmount(), f.getPaidAmount(), f.getAmount().subtract(f.getPaidAmount()),
                    f.getDueDate(), f.getStatus());
        }
    }

    public record PaymentResponse(
            UUID id, UUID feeId, UUID studentId, BigDecimal amount,
            PaymentMethod method, String reference, LocalDate paidOn) {
        public static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.getId(), p.getFeeId(), p.getStudentId(), p.getAmount(),
                    p.getMethod(), p.getReference(), p.getPaidOn());
        }
    }

    public record FeeSummary(
            UUID studentId, BigDecimal totalBilled, BigDecimal totalPaid, BigDecimal totalDue,
            List<FeeResponse> fees) {
    }
}
