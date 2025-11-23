package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.commands.RegisterAiFailure;
import br.com.ocr.ocr_api.commands.RegisterAiResult;
import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import br.com.ocr.ocr_api.domain.AnalyzedDocument;
import br.com.ocr.ocr_api.infra.ReceiptAnalysisRepository;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import br.com.ocr.ocr_api.service.ai.AiProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ReceiptAnalysisRepository repository;
    private final AiProcessor aiProcessor;
    private final CommandGateway command;

    public void startAnalysis(String jobId, AnalyzedDocument document) throws IOException {
        aiProcessor.analyzeItemList(document.getLineItems())
                .thenAccept((result) -> {
                    command.send(new RegisterAiResult(jobId, result));
                })
                .exceptionally(e -> {
                    log.error("AI analysis failed for jobId {}, {}", jobId, e.getMessage());
                    command.send(new RegisterAiFailure(jobId, e.getMessage()));
                    return null;
                });
    }

    public List<AiAnalyzedItem> getAnalysis(String jobId) {
        return repository.findById(jobId).map(ReceiptAnalysis::getAiResult).orElseThrow();
    }
}
