package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.domain.AnalyzedDocument;
import br.com.ocr.ocr_api.domain.AnalysisStatus;

import java.time.Instant;

public record AiAnalysisRequested(String jobId, AnalyzedDocument analyzedDocument,
                                  AnalysisStatus status, Instant ts) {
}
