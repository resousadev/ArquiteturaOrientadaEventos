package io.resousadev.linuxtips.managerfile.controller;

import io.resousadev.linuxtips.common.dto.ApiResponse;
import io.resousadev.linuxtips.common.dto.FileMetadataDto;
import io.resousadev.linuxtips.managerfile.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for file management operations.
 * Provides API endpoints for upload, download, delete and list files.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Upload a file.
     *
     * @param file the file to upload
     * @param metadata optional metadata as key-value pairs
     * @return file metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileMetadataDto>> uploadFile(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "metadata", required = false) final Map<String, String> metadata) {

        log.debug("Upload request: fileName={}, size={}, contentType={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        final FileMetadataDto result = fileService.uploadFile(file, metadata);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "File uploaded successfully"));
    }

    /**
     * Get file metadata by ID.
     *
     * @param fileId the file identifier
     * @return file metadata
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataDto>> getFileMetadata(
            @PathVariable final String fileId) {

        log.debug("Get metadata request: fileId={}", fileId);

        final FileMetadataDto result = fileService.getFileMetadata(fileId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Delete a file by ID.
     *
     * @param fileId the file identifier
     * @return success response
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable final String fileId) {
        log.debug("Delete request: fileId={}", fileId);

        fileService.deleteFile(fileId);

        return ResponseEntity.ok(ApiResponse.success(null, "File deleted successfully"));
    }

    /**
     * List all files.
     *
     * @return list of file metadata
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FileMetadataDto>>> listFiles() {
        log.debug("List files request");

        final List<FileMetadataDto> files = fileService.listFiles();

        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * Generate a pre-signed download URL for a file.
     *
     * @param fileId the file identifier
     * @return pre-signed URL
     */
    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(@PathVariable final String fileId) {
        log.debug("Generate download URL request: fileId={}", fileId);

        final String url = fileService.generateDownloadUrl(fileId);

        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
