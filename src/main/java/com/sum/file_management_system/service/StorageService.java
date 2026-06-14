package com.sum.file_management_system.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sum.file_management_system.entity.Document;

public interface StorageService {
    String upload(
            MultipartFile file
    );

    String download(
            String objectKey
    );

    String delete(
            String objectKey
    );

    List<Document> listAllDocuments();
}
