package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.dto.AiAnalyzedItem;

import java.time.Instant;
import java.util.List;

public record AiAnalysisCompleted(
        String jobId,
        String ocrJobId,
        String aiJobId,
        List<AiAnalyzedItem> aiResult,
        Instant ts

) {}
