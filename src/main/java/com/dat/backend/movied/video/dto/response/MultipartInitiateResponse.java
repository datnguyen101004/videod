package com.dat.backend.movied.video.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipartInitiateResponse {
    private String uploadId;
    private String key;
    private long chunkSize;
}
