package com.ims.file;

import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB

    private final StoredFileRepository repository;

    public FileService(StoredFileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StoredFile upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("File too large (max 5 MB)");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        try {
            StoredFile sf = new StoredFile();
            sf.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
            sf.setContentType(ct);
            sf.setSizeBytes(file.getSize());
            sf.setData(file.getBytes());
            return repository.save(sf);
        } catch (IOException e) {
            throw new BadRequestException("Could not read file");
        }
    }

    /** Read by id. No tenant guard: downloads are public by unguessable UUID (for &lt;img&gt; tags). */
    @Transactional(readOnly = true)
    public StoredFile get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));
    }
}
