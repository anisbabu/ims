package com.ims.file;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /** Upload an image. Returns the id and the public URL to embed. */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        StoredFile sf = fileService.upload(file);
        return Map.of("id", sf.getId().toString(), "url", "/api/files/" + sf.getId());
    }

    /** Public download by unguessable id (so it can be used directly as an image src). */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        StoredFile sf = fileService.get(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(sf.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(sf.getData());
    }
}
