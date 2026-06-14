package com.sum.file_management_system.service.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sum.file_management_system.entity.Document;
import com.sum.file_management_system.repository.DocumentRepository;
import com.sum.file_management_system.service.StorageService;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final DocumentRepository documentRepository;

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
                        .bucket("documents")
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

    public List<Document> listAllDocuments() {
        return documentRepository.findAll();
    }

}
