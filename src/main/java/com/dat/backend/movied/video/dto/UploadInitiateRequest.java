package com.dat.backend.movied.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadInitiateRequest {
    private String filename;
    private String contentType;
    private long fileSize;

    private String category; // It needed for create key
}
