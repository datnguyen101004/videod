package com.dat.backend.movied.video.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String category;
    private String authorName;
    private Instant created_at;
    private Instant updated_at;
}
