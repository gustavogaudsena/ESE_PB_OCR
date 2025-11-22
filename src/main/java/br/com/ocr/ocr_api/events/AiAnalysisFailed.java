package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.domain.AnalysisStatus;

import java.time.Instant;

public record AiAnalysisFailed(
        String jobId,
        String message,
        AnalysisStatus status,
        Instant ts

) {}
