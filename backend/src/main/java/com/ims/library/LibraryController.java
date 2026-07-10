package com.ims.library;

import com.ims.common.PageResponse;
import com.ims.library.dto.LibraryDtos.BookIssueResponse;
import com.ims.library.dto.LibraryDtos.BookResponse;
import com.ims.library.dto.LibraryDtos.CreateBook;
import com.ims.library.dto.LibraryDtos.IssueBook;
import com.ims.library.dto.LibraryDtos.ReturnBook;
import com.ims.library.dto.LibraryDtos.UpdateBook;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/library")
public class LibraryController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public BookResponse createBook(@Valid @RequestBody CreateBook req) {
        return libraryService.createBook(req);
    }

    @GetMapping("/books")
    public PageResponse<BookResponse> listBooks(@RequestParam(required = false) String q, Pageable pageable) {
        return libraryService.listBooks(q, pageable);
    }

    @GetMapping("/books/{id}")
    public BookResponse getBook(@PathVariable UUID id) {
        return libraryService.getBook(id);
    }

    @PutMapping("/books/{id}")
    @PreAuthorize(STAFF)
    public BookResponse updateBook(@PathVariable UUID id, @Valid @RequestBody UpdateBook req) {
        return libraryService.updateBook(id, req);
    }

    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(STAFF)
    public void deleteBook(@PathVariable UUID id) {
        libraryService.deleteBook(id);
    }

    @PostMapping("/issues")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public BookIssueResponse issue(@Valid @RequestBody IssueBook req) {
        return libraryService.issue(req);
    }

    @GetMapping("/issues")
    public PageResponse<BookIssueResponse> listIssues(@RequestParam(required = false) BookIssueStatus status,
                                                      @RequestParam(required = false) UUID studentId,
                                                      Pageable pageable) {
        return libraryService.listIssues(status, studentId, pageable);
    }

    @PatchMapping("/issues/{id}/return")
    @PreAuthorize(STAFF)
    public BookIssueResponse returnBook(@PathVariable UUID id, @RequestBody(required = false) ReturnBook req) {
        return libraryService.returnBook(id, req != null ? req.fine() : null);
    }
}
