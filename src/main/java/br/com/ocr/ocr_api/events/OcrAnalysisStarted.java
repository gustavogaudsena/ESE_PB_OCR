package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.domain.AnalysisStatus;

import java.time.Instant;

public record OcrAnalysisStarted(String jobId, String fileIdentifier, AnalysisStatus status, Instant ts) {
}
