package br.com.ocr.ocr_api.service.ai;

import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import br.com.ocr.ocr_api.domain.LineItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiProcessor {
    CompletableFuture<List<AiAnalyzedItem>> analyzeItemList(List<LineItem> lineItems, String jobId);
}
