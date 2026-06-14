package com.sum.file_management_system.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {

    @Value("${minio.user}")
    private String minioUser;

    @Value("${minio.password}")
    private String minioPassword;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
            .endpoint("http://localhost:9000")
            .credentials(
                minioUser,
                minioPassword
            )
            .build();
    }
}