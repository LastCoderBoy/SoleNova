package com.jk.productcatalog.config.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class AwsS3Config {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * S3 Client Bean
     * Used for all S3 operations (upload, download, delete, list)
     */
    @Bean
    public S3Client s3Client() {
        try {
            log.info("[S3-CONFIG] Initializing S3 Client for region: {}, bucket: {}", region, bucketName);

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            S3Client client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            log.info("[S3-CONFIG] S3 Client initialized successfully");
            return client;

        } catch (Exception e) {
            log.error("[S3-CONFIG] Failed to initialize S3 Client: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize S3 Client", e);
        }
    }
}
