package com.ims.fee.dto;

import com.ims.fee.FeeStructure;
import com.ims.fee.OnlinePayment;
import com.ims.fee.OnlinePaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FeeStructureDtos {

    private FeeStructureDtos() {
    }

    // ---- Fee structures ----

    public record SaveFeeStructure(
            @NotNull UUID academicYearId,
            @NotNull UUID gradeId,
            @NotBlank String title,
            @NotNull @Positive BigDecimal amount,
            LocalDate dueDate) {
    }

    public record FeeStructureResponse(
            UUID id, UUID academicYearId, UUID gradeId, String title,
            BigDecimal amount, LocalDate dueDate) {
        public static FeeStructureResponse from(FeeStructure s) {
            return new FeeStructureResponse(s.getId(), s.getAcademicYearId(), s.getGradeId(),
                    s.getTitle(), s.getAmount(), s.getDueDate());
        }
    }

    public record GenerateFees(
            @NotNull UUID academicYearId,
            @NotNull UUID gradeId) {
    }

    public record GenerateResult(int students, int created, int skipped) {
    }

    // ---- Online payments ----

    public record CheckoutRequest(
            @NotNull UUID feeId,
            /** Null = pay the full outstanding due. */
            @Positive BigDecimal amount) {
    }

    public record OnlinePaymentResponse(
            UUID id, UUID feeId, String feeTitle, UUID studentId, BigDecimal amount,
            String provider, OnlinePaymentStatus status, String reference, Instant paidAt,
            String checkoutUrl) {
        public static OnlinePaymentResponse from(OnlinePayment p, String feeTitle, String checkoutUrl) {
            return new OnlinePaymentResponse(p.getId(), p.getFeeId(), feeTitle, p.getStudentId(),
                    p.getAmount(), p.getProvider(), p.getStatus(), p.getReference(), p.getPaidAt(),
                    checkoutUrl);
        }
    }
}
