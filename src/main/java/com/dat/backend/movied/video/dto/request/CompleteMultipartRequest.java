package com.dat.backend.movied.video.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteMultipartRequest {
    private String key;
    private String uploadId;
    private List<CompletedPartDto> parts;

    private String title;
    private String category;
    private String description;
}
