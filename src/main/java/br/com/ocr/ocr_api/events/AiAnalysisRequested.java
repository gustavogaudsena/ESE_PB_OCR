package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.dto.AnalyzedDocument;
import br.com.ocr.ocr_api.model.AnalysisStatus;

import java.time.Instant;

public record AiAnalysisRequested(String jobId, String ocrJobId, AnalyzedDocument analyzedDocument,
                                  AnalysisStatus status, Instant ts) {
}
