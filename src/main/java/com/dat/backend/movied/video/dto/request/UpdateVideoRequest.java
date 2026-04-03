package com.dat.backend.movied.video.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVideoRequest {
    private Long videoId;
    private String title;
    private String description;
}
