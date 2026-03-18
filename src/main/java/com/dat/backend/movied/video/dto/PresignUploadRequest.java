package com.dat.backend.movied.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignUploadRequest {
    private String filename;
    private String contentType;
    private Long fileSize;

    private String category;
}
