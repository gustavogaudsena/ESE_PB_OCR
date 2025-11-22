package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.domain.AnalysisStatus;

import java.time.Instant;

public record OcrAnalysisRegistered(String jobId, String ocrJobId, AnalysisStatus status, Instant ts) {
}
