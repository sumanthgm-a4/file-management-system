package com.sum.file_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sum.file_management_system.dto.UploadRequest;
import com.sum.file_management_system.entity.Document;
import com.sum.file_management_system.service.StorageService;
import com.sum.file_management_system.service.impl.MinioStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/documents")
public class DocumentController {

    private final StorageService storageService;

    @PostMapping(path = "/upload")
    public String uploadDocument(@RequestBody UploadRequest request) {
        return storageService.upload(request);
    }

    @GetMapping
    public List<Document> listAllDocumentsMetadata() {
        return storageService.listAllDocuments();
    }

    @GetMapping(path = "/download")
    public String downloadDocument(@RequestParam("key") String objectKey) {
        return storageService.download(objectKey);
    }

    @DeleteMapping(path = "/delete")
    public String deleteDocument(@RequestParam("key") String objectKey) {
        return storageService.delete(objectKey);
    }
}
