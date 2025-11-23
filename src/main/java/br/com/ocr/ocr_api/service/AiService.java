package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.commands.RegisterAiAnalysisFailed;
import br.com.ocr.ocr_api.commands.RegisterAiResult;
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
                    command.send(new RegisterAiAnalysisFailed(jobId, e.getMessage()));
                    log.error("AI analysis failed for jobId {}", jobId);
                    return null;
                });
    }

    public void startAnalysis(String jobId) throws IOException {
        ReceiptAnalysis job = repository.findById(jobId).orElseThrow(() -> new IOException("Job not found in database: " + jobId));

        aiProcessor.analyzeItemList(job.getOcrResult().getLineItems())
                .thenAccept((result) -> {
                    command.send(new RegisterAiResult(jobId, result));
                })
                .exceptionally(e -> {
                    command.send(new RegisterAiAnalysisFailed(jobId, e.getMessage()));
                    log.error("AI analysis failed for jobId {}", jobId);
                    return null;
                });
    }

    public ReceiptAnalysis getAnalysis(String jobId) {
        return repository.findById(jobId).orElseThrow();
    }
}
