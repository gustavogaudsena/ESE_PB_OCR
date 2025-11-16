package br.com.ocr.ocr_api.events;

import java.time.Instant;

public record AiAnalysisCompleted(
        String jobId,
        String ocrJobId,
        String aiJobId,
        Instant ts

) {}
