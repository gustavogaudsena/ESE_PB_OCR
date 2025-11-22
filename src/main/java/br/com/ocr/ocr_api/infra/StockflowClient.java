package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "stockflow")
public interface StockflowClient {

    @PostMapping("/transacao/nota")
    void createTransactionByList(@RequestBody List<AiAnalyzedItem> request);
}
