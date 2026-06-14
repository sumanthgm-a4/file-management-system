package com.sum.file_management_system.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sum.file_management_system.entity.Document;
import com.sum.file_management_system.repository.DocumentRepository;
import com.sum.file_management_system.service.StorageService;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    @Transactional
    public String upload(MultipartFile file) {
                
        try {
            String objectKey = UUID.randomUUID()
            + "-"
            + file.getOriginalFilename();

            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setObjectKey(objectKey);
            document.setSize(file.getSize());

            documentRepository.save(document);

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(
                        file.getInputStream(),
                        file.getSize(),
                        -1
                    )
                    .build()
            );

            return objectKey;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public List<Document> listAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public String download(String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(10, TimeUnit.MINUTES)
                    .build()
            );

            return url;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    @Override
    @Transactional
    public String delete(String objectKey) {
        try {
            documentRepository.findByObjectKey(objectKey)
                .orElseThrow(() -> new RuntimeException("Object doesn't exist"));

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()
            );

            return "Object deleted successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Object deletion is unsuccessful";
    }

}
