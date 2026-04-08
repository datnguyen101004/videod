package com.dat.backend.movied.video.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagesResponse {
    List<VideoResponse> videoResponses;
    String cursor;
    Boolean hasMore;
}
