package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.dto.AiAnalyzedItem;
import br.com.ocr.ocr_api.model.AnalysisStatus;

import java.time.Instant;
import java.util.List;

public record AiAnalysisFailed(
        String jobId,
        String message,
        AnalysisStatus status,
        Instant ts

) {}
