package com.ims.library;

import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.library.dto.LibraryDtos.BookIssueResponse;
import com.ims.library.dto.LibraryDtos.BookResponse;
import com.ims.library.dto.LibraryDtos.CreateBook;
import com.ims.library.dto.LibraryDtos.IssueBook;
import com.ims.library.dto.LibraryDtos.UpdateBook;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BookIssueRepository issueRepository;
    private final StudentRepository studentRepository;

    public LibraryService(BookRepository bookRepository,
                          BookIssueRepository issueRepository,
                          StudentRepository studentRepository) {
        this.bookRepository = bookRepository;
        this.issueRepository = issueRepository;
        this.studentRepository = studentRepository;
    }

    // ---- Books ----

    @Transactional
    public BookResponse createBook(CreateBook req) {
        Book b = new Book();
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(req.isbn());
        b.setCategory(req.category());
        b.setShelf(req.shelf());
        int copies = Math.max(req.totalCopies(), 0);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        return BookResponse.from(bookRepository.save(b));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> listBooks(String q, Pageable pageable) {
        Page<Book> page = StringUtils.hasText(q)
                ? bookRepository.findByTitleContainingIgnoreCase(q, pageable)
                : bookRepository.findAll(pageable);
        return PageResponse.from(page, BookResponse::from);
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(UUID id) {
        return BookResponse.from(requireBook(id));
    }

    @Transactional
    public BookResponse updateBook(UUID id, UpdateBook req) {
        Book b = requireBook(id);
        if (req.title() != null) b.setTitle(req.title());
        if (req.author() != null) b.setAuthor(req.author());
        if (req.isbn() != null) b.setIsbn(req.isbn());
        if (req.category() != null) b.setCategory(req.category());
        if (req.shelf() != null) b.setShelf(req.shelf());
        if (req.totalCopies() != null) {
            int delta = req.totalCopies() - b.getTotalCopies();
            b.setTotalCopies(req.totalCopies());
            b.setAvailableCopies(Math.max(0, b.getAvailableCopies() + delta));
        }
        return BookResponse.from(b);
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.delete(requireBook(id));
    }

    // ---- Issues ----

    @Transactional
    public BookIssueResponse issue(IssueBook req) {
        Book book = requireBook(req.bookId());
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No copies available");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BookIssue i = new BookIssue();
        i.setBookId(book.getId());
        i.setStudentId(req.studentId());
        i.setIssueDate(LocalDate.now());
        i.setDueDate(req.dueDate() != null ? req.dueDate() : LocalDate.now().plusDays(14));
        i.setStatus(BookIssueStatus.ISSUED);
        return BookIssueResponse.from(issueRepository.save(i));
    }

    @Transactional
    public BookIssueResponse returnBook(UUID issueId, BigDecimal fine) {
        BookIssue i = issueRepository.findById(issueId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Issue not found"));
        if (i.getStatus() == BookIssueStatus.RETURNED) {
            throw new BadRequestException("Book already returned");
        }
        i.setStatus(BookIssueStatus.RETURNED);
        i.setReturnDate(LocalDate.now());
        if (fine != null) i.setFine(fine);
        Book book = requireBook(i.getBookId());
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        return BookIssueResponse.from(i);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookIssueResponse> listIssues(BookIssueStatus status, UUID studentId, Pageable pageable) {
        Page<BookIssue> page;
        if (studentId != null) {
            page = issueRepository.findByStudentId(studentId, pageable);
        } else if (status != null) {
            page = issueRepository.findByStatus(status, pageable);
        } else {
            page = issueRepository.findAll(pageable);
        }
        return PageResponse.from(page, BookIssueResponse::from);
    }

    private Book requireBook(UUID id) {
        return bookRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Book not found"));
    }
}
