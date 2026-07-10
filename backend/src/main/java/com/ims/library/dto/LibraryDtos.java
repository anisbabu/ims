package com.ims.library.dto;

import com.ims.library.Book;
import com.ims.library.BookIssue;
import com.ims.library.BookIssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class LibraryDtos {

    private LibraryDtos() {
    }

    public record CreateBook(
            @NotBlank String title,
            String author,
            String isbn,
            String category,
            String shelf,
            @PositiveOrZero int totalCopies) {
    }

    public record UpdateBook(
            String title, String author, String isbn, String category, String shelf,
            Integer totalCopies) {
    }

    public record BookResponse(
            UUID id, String title, String author, String isbn, String category, String shelf,
            int totalCopies, int availableCopies) {
        public static BookResponse from(Book b) {
            return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getCategory(), b.getShelf(), b.getTotalCopies(), b.getAvailableCopies());
        }
    }

    public record IssueBook(
            @NotNull UUID bookId,
            @NotNull UUID studentId,
            LocalDate dueDate) {
    }

    public record ReturnBook(BigDecimal fine) {
    }

    public record BookIssueResponse(
            UUID id, UUID bookId, UUID studentId, LocalDate issueDate, LocalDate dueDate,
            LocalDate returnDate, BookIssueStatus status, BigDecimal fine) {
        public static BookIssueResponse from(BookIssue i) {
            return new BookIssueResponse(i.getId(), i.getBookId(), i.getStudentId(), i.getIssueDate(),
                    i.getDueDate(), i.getReturnDate(), i.getStatus(), i.getFine());
        }
    }
}
