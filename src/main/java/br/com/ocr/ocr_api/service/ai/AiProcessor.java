package br.com.ocr.ocr_api.service.ai;

import br.com.ocr.ocr_api.dto.LineItem;
import br.com.ocr.ocr_api.model.AiJob;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiProcessor {
    CompletableFuture<AiJob> analyzeItemList(List<LineItem> lineItems, String jobId);
}
