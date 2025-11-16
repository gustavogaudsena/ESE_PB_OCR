package br.com.ocr.ocr_api.events;

import java.time.Instant;

public record OcrAnalysisRegistered(String jobId, String ocrJobId, Instant ts) {
}
