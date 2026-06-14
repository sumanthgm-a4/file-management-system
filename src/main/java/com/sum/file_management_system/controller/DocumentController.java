package com.sum.file_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sum.file_management_system.entity.Document;
import com.sum.file_management_system.service.impl.MinioStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/documents")
public class DocumentController {

    private final MinioStorageService minioStorageService;

    @PostMapping(path = "/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        return minioStorageService.upload(file);
    }

    @GetMapping
    public List<Document> listAllDocuments() {
        return minioStorageService.listAllDocuments();
    }
}
