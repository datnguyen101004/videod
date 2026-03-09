package com.dat.backend.movied.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseApi<T> {
    private int statusCode;
    private String message;
    private Object data;

    public static <T> ResponseApi<T> success(T data) {
        return ResponseApi.<T>builder()
                .statusCode(200)
                .data(data)
                .message("success")
                .build();
    }

    public static <T> ResponseApi<T> success() {
        return success(null);
    }

    public static <T> ResponseApi<T> fail(int statusCode, String message) {
        return ResponseApi.<T>builder()
                .statusCode(statusCode)
                .message(message).build();
    }
}
