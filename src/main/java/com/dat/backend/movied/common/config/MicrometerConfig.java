package com.dat.backend.movied.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MicrometerConfig {

    private final MeterRegistry meterRegistry;
    private final Counter successUploadVideoCounter;
    private final Counter failedUploadVideoCounter;

    public MicrometerConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.successUploadVideoCounter = Counter.builder("video.upload")
                .tag("status", "success")
                .description("Upload success video")
                .register(meterRegistry);

        this.failedUploadVideoCounter = Counter.builder("video.upload")
                .tag("status", "failed")
                .description("Failed upload video")
                .register(meterRegistry);
    }

    public void incrementSuccessUploadVideoCounter() {
        successUploadVideoCounter.increment();
    }

    public void incrementFailedUploadVideoCounter() {
        failedUploadVideoCounter.increment();
    }

}
