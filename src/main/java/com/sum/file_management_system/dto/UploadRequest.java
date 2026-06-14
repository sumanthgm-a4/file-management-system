package com.sum.file_management_system.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UploadRequest {
    private String fileName;  
}
