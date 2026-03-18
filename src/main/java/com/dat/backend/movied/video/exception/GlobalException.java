package com.dat.backend.movied.video.exception;

import com.dat.backend.movied.common.dto.ResponseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(VideoUploadException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResponseApi<?> videoUploadException(VideoUploadException e) {
        return ResponseApi.fail(HttpStatus.REQUEST_TIMEOUT.value(), e.getMessage());
    }

    @ExceptionHandler(ResourceNotExit.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApi<?> resourceNotExit(ResourceNotExit e) {
        return ResponseApi.fail(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseApi<?> s3Exception(S3Exception e) {
        return ResponseApi.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApi<?> exception(Exception e) {
        return ResponseApi.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
}
