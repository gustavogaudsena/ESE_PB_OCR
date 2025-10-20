package br.com.ocr.ocr_api.service.ai;

import br.com.ocr.ocr_api.dto.LineItem;

import java.util.List;

public interface AiProcessorInterface {
    void analyzeItemList(List<LineItem> lineItems, String jobId);
}
