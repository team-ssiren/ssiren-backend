package com.ssaika.ssiren.global.config;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    public S3Client r2S3Client(R2Properties properties) {
        properties.validateRequired();

        S3Client s3Client = S3Client.builder()
            .endpointOverride(URI.create(properties.endpoint()))
            .region(Region.of(properties.resolvedRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    properties.accessKeyId(),
                    properties.secretAccessKey()
                )
            ))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())
            .build();

        s3Client.headBucket(builder -> builder.bucket(properties.bucket()));

        return s3Client;
    }
}
