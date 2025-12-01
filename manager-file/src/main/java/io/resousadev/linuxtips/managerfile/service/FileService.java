package io.resousadev.linuxtips.managerfile.service;

import io.resousadev.linuxtips.common.dto.FileMetadataDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service interface for file management operations.
 */
public interface FileService {

    /**
     * Upload a file to storage.
     *
     * @param file the file to upload
     * @param metadata optional metadata
     * @return file metadata after upload
     */
    FileMetadataDto uploadFile(MultipartFile file, Map<String, String> metadata);

    /**
     * Get file metadata by ID.
     *
     * @param fileId the file identifier
     * @return file metadata
     */
    FileMetadataDto getFileMetadata(String fileId);

    /**
     * Delete a file by ID.
     *
     * @param fileId the file identifier
     */
    void deleteFile(String fileId);

    /**
     * List all files.
     *
     * @return list of file metadata
     */
    List<FileMetadataDto> listFiles();

    /**
     * Generate a pre-signed URL for file download.
     *
     * @param fileId the file identifier
     * @return pre-signed URL
     */
    String generateDownloadUrl(String fileId);
}
