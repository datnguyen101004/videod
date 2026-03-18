package com.dat.backend.movied.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyUploadPresign {
    private String key;
    private String title;
    private String description;
    private String author;
    private String category;
}
