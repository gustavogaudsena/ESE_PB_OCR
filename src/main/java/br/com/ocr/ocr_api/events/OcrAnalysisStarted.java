package br.com.ocr.ocr_api.events;

import java.time.Instant;

public record OcrAnalysisStarted(String jobId, String fileIdentifier, Instant ts) {
}
