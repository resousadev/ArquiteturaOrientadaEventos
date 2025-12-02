package io.resousadev.linuxtips.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for file metadata shared between microservices.
 * Used for REST API responses and EventBridge payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {

    @JsonProperty("fileId")
    private String fileId;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("contentType")
    private String contentType;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("storageLocation")
    private String storageLocation;

    @JsonProperty("uploadedBy")
    private String uploadedBy;

    @JsonProperty("uploadedAt")
    private Instant uploadedAt;

    @JsonProperty("metadata")
    private java.util.Map<String, String> metadata;
}
