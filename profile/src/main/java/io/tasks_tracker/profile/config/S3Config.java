package io.tasks_tracker.profile.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3Config 
{
    @Value("${cloud.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.s3.region}")
    private String region;

    @Value("${cloud.s3.access-key}")
    private String accessKey;

    @Value("${cloud.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client()
    {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        accessKey, secretKey
                    )
                ))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .chunkedEncodingEnabled(false)
                    .checksumValidationEnabled(true)
                    .build())
                .endpointOverride(URI.create(endpoint))
                .build();
    }
}
