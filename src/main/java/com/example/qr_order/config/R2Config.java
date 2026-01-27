package com.example.qr_order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Bean
    public S3Client s3Client() {
        // Endpoint của R2: https://<account_id>.r2.cloudflarestorage.com
        String endpointUrl = String.format("https://%s.r2.cloudflarestorage.com", accountId);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto")) // R2 tự động nhận region, để "auto" hoặc "us-east-1"
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Quan trọng cho R2
                        .build())
                .build();
    }
}