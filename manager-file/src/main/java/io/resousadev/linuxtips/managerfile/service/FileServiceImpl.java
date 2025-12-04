package io.resousadev.linuxtips.managerfile.service;

import io.resousadev.linuxtips.common.dto.FileMetadataDto;
import io.resousadev.linuxtips.common.event.EventTypes;
import io.resousadev.linuxtips.common.exception.ResourceNotFoundException;
import io.resousadev.linuxtips.managerfile.producer.FileEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of FileService using AWS S3 for storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileEventProducer eventProducer;

    @Value("${aws.s3.bucket:manager-file-bucket}")
    private String bucketName;

    @Value("${aws.s3.presign-duration:60}")
    private int presignDurationMinutes;

    @Override
    public FileMetadataDto uploadFile(final MultipartFile file, final Map<String, String> metadata) {
        final String fileId = UUID.randomUUID().toString();
        final String key = buildS3Key(fileId, file.getOriginalFilename());

        try {
            final String checksum = calculateChecksum(file.getBytes());

            final PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .metadata(metadata != null ? metadata : Map.of())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            final FileMetadataDto fileMetadata = FileMetadataDto.builder()
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .checksum(checksum)
                    .storageLocation(key)
                    .uploadedAt(Instant.now())
                    .metadata(metadata)
                    .build();

            // Publish event
            eventProducer.publishFileEvent(EventTypes.FILE_UPLOADED, fileMetadata);

            log.info("File uploaded: fileId={}, fileName={}, size={}, bucket={}", 
                    fileId, file.getOriginalFilename(), file.getSize(), bucketName);

            return fileMetadata;
        } catch (IOException e) {
            log.error("File upload failed: fileName={}, bucket={}, error={}", 
                    file.getOriginalFilename(), bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public FileMetadataDto getFileMetadata(final String fileId) {
        try {
            final String prefix = fileId + "/";
            final var listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(1)
                    .build();

            final var response = s3Client.listObjectsV2(listRequest);

            if (response.contents().isEmpty()) {
                throw new ResourceNotFoundException("File", fileId);
            }

            final var s3Object = response.contents().get(0);
            final var headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Object.key())
                    .build();

            final var headResponse = s3Client.headObject(headRequest);

            return FileMetadataDto.builder()
                    .fileId(fileId)
                    .fileName(extractFileName(s3Object.key()))
                    .contentType(headResponse.contentType())
                    .size(s3Object.size())
                    .storageLocation(s3Object.key())
                    .uploadedAt(s3Object.lastModified())
                    .metadata(headResponse.metadata())
                    .build();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("File", fileId);
        }
    }

    @Override
    public void deleteFile(final String fileId) {
        final FileMetadataDto metadata = getFileMetadata(fileId);

        final DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(metadata.getStorageLocation())
                .build();

        s3Client.deleteObject(request);

        // Publish event
        eventProducer.publishFileEvent(EventTypes.FILE_DELETED, metadata);

        log.info("File deleted: fileId={}, bucket={}", fileId, bucketName);
    }

    @Override
    public List<FileMetadataDto> listFiles() {
        final var request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return s3Client.listObjectsV2(request).contents().stream()
                .map(obj -> FileMetadataDto.builder()
                        .fileId(extractFileId(obj.key()))
                        .fileName(extractFileName(obj.key()))
                        .size(obj.size())
                        .storageLocation(obj.key())
                        .uploadedAt(obj.lastModified())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String generateDownloadUrl(final String fileId) {
        final FileMetadataDto metadata = getFileMetadata(fileId);

        final GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(metadata.getStorageLocation())
                .build();

        final GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                .getObjectRequest(getRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String buildS3Key(final String fileId, final String fileName) {
        return fileId + "/" + fileName;
    }

    private String extractFileId(final String key) {
        final int slashIndex = key.indexOf('/');
        return slashIndex > 0 ? key.substring(0, slashIndex) : key;
    }

    private String extractFileName(final String key) {
        final int slashIndex = key.lastIndexOf('/');
        return slashIndex >= 0 ? key.substring(slashIndex + 1) : key;
    }

    private String calculateChecksum(final byte[] data) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
}
