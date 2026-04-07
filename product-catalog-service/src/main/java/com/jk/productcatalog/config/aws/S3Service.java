package com.jk.productcatalog.config.aws;

import com.jk.commonlibrary.exception.InternalServerException;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.base-path}")
    private String basePath;

    /**
     * Upload product image with unique filename
     */
    public UploadResult uploadProductImage(MultipartFile file, Long productId, Integer displayOrder) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        String s3Key = String.format("%s/products/%d/%s", basePath, productId, uniqueFilename);

        String publicUrl = uploadToS3(file, s3Key);

        return UploadResult.builder()
                .s3Key(s3Key)
                .url(publicUrl)
                .displayOrder(displayOrder)
                .build();
    }

    /**
     * Upload variant-specific image
     */
    public UploadResult uploadVariantImage(MultipartFile file, Long productId, Long variantId, Integer displayOrder) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        String s3Key = String.format("%s/products/%d/variants/%d/%s",
                basePath, productId, variantId, uniqueFilename);

        String publicUrl = uploadToS3(file, s3Key);

        return UploadResult.builder()
                .s3Key(s3Key)
                .url(publicUrl)
                .displayOrder(displayOrder)
                .build();
    }

    /**
     * Upload category banner
     */
    public String uploadCategoryImage(MultipartFile file, Long categoryId) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        String s3Key = String.format("%s/categories/%d/%s", basePath, categoryId, uniqueFilename);

        return uploadToS3(file, s3Key);
    }

    /**
     * Upload brand logo
     */
    public String uploadBrandLogo(MultipartFile file, Long brandId) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        String s3Key = String.format("%s/brands/%d/%s", basePath, brandId, uniqueFilename);

        return uploadToS3(file, s3Key);
    }


    /**
     * Reads a file from S3 and returns its content as a byte array.
     *
     * @param s3Key the key of the file to read
     */
    public byte[] readFile(String s3Key) {
        try {
            log.info("[S3-SERVICE] Reading file from S3: {}", s3Key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
                byte[] fileContent = s3ObjectStream.readAllBytes();
                log.info("[S3-SERVICE] Successfully read {} from S3 ({} bytes)", s3Key, fileContent.length);
                return fileContent;
            }

        } catch (NoSuchKeyException e) {
            log.error("[S3-SERVICE] File not found in S3: {}", s3Key);
            throw new ResourceNotFoundException("File not found in S3:  " + s3Key);
        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error reading file {}: {}", s3Key, e.awsErrorDetails().errorMessage());
            throw new InternalServerException("Failed to read file from S3");
        } catch (IOException e) {
            log.error("[S3-SERVICE] IO error reading file {}: {}", s3Key, e.getMessage());
            throw new InternalServerException("Failed to read file content from S3");
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error reading file {}: {}", s3Key, e.getMessage(), e);
            throw new InternalServerException("Unexpected error reading file from S3");
        }
    }

    /**
     * Deletes a file from S3 using its URL.
     *
     * @param fileUrl the public URL of the file
     */
    public void deleteFileFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            log.warn("Invalid S3 URL provided for deletion or URL is empty: {}", fileUrl);
            return;
        }

        try {
            // Extract the key from the public URL
            String domainPattern = ".amazonaws.com/";
            int startIndex = fileUrl.indexOf(domainPattern) + domainPattern.length();
            String key = fileUrl.substring(startIndex);
            
            log.debug("Deleting file from S3 bucket: {}, key: {}", bucketName, key);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", fileUrl, e);
            throw new InternalServerException("Failed to delete file from S3", e);
        }
    }


    /**
     * Deletes a file from S3 using its S3 key.
     *
     * @param s3Key the key of the file to delete
     */
    public void deleteFile(String s3Key) {
        try {
            log.info("[S3-SERVICE] Deleting file from S3: {}", s3Key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("[S3-SERVICE] Successfully deleted {} from S3 bucket {}", s3Key, bucketName);

        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error deleting file {}: {}", s3Key, e.awsErrorDetails().errorMessage());
            throw new InternalServerException("Failed to delete file from S3");
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error deleting file {}: {}", s3Key, e.getMessage(), e);
            throw new InternalServerException("Unexpected error deleting file from S3");
        }
    }

    /**
     * Delete multiple files in batch
     */
    public void deleteFiles(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder()
                            .objects(s3Keys.stream()
                                    .map(key -> ObjectIdentifier.builder().key(key).build())
                                    .collect(Collectors.toList()))
                            .build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);
            log.info("[S3-SERVICE] Deleted {} files from S3", response.deleted().size());

            if (!response.errors().isEmpty()) {
                log.warn("[S3-SERVICE] Some files failed to delete: {}", response.errors());
            }

        } catch (Exception e) {
            log.error("[S3-SERVICE] Error batch deleting files", e);
            throw new InternalServerException("Failed to delete files from S3", e);
        }
    }

    // ==================================================
    //                 HELPER METHODS
    // ==================================================


    /**
     * Core upload method with content type handling
     */
    private String uploadToS3(MultipartFile file, String s3Key) {
        try {
            String contentType = determineContentType(file);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)  // Since bucket is public-read
                    .build();

            log.debug("[S3-SERVICE] Uploading file to S3 bucket: {}, key: {}", bucketName, s3Key);
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, s3Key);
            log.info("[S3-SERVICE] File uploaded successfully to S3: {}", publicUrl);

            return publicUrl;

        } catch (IOException e) {
            log.error("Error reading file during S3 upload", e);
            throw new InternalServerException("Failed to upload file to S3", e);
        } catch (SdkClientException e) {
            log.error("[S3-SERVICE] SDK client error uploading file {}: {}", s3Key, e.getMessage());
            throw new InternalServerException("Failed to communicate with S3", e);
        } catch (Exception e) {
            log.error("[S3-SERVICE] Error uploading file to S3", e);
            throw new InternalServerException("Failed to upload file to S3", e);
        }
    }

    /**
     * Determine content type from file or fallback to extension
     */
    private String determineContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("application/octet-stream")) {
            return contentType;
        }

        // Fallback based on extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".webp")) return "image/webp";
            if (filename.endsWith(".gif")) return "image/gif";
        }

        return "image/jpeg"; // default
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @Data
    @Builder
    public static class UploadResult {
        private String s3Key;
        private String url;
        private Integer displayOrder;
    }
}
