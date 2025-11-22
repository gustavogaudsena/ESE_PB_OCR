package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.dto.AnalyzedDocument;

import java.time.Instant;

public record AiAnalysisRequested(
        String jobId,
        String ocrJobId,
        AnalyzedDocument analyzedDocument,
        Instant ts
) {}
