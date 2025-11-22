package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import br.com.ocr.ocr_api.domain.AnalysisStatus;

import java.time.Instant;
import java.util.List;

public record AiAnalysisCompleted(
        String jobId,
        List<AiAnalyzedItem> aiResult,
        AnalysisStatus status,
        Instant ts

) {}
