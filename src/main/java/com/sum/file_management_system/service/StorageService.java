package com.sum.file_management_system.service;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(
            MultipartFile file
    );

    // InputStream download(
    //         String objectKey
    // );

    // void delete(
    //         String objectKey
    // );
}
