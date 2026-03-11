package com.dat.backend.movied.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDownloadRequest {
    private Long videoId;
    private String destinationPath;
}
