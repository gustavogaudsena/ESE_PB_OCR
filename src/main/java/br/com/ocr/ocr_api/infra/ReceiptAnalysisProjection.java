package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.events.*;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class ReceiptAnalysisProjection {

    private final ReceiptAnalysisRepository repository;

    public ReceiptAnalysisProjection(ReceiptAnalysisRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OcrAnalysisRequested event) {
        ReceiptAnalysis receiptAnalysis = new ReceiptAnalysis();
        receiptAnalysis.setId(event.jobId());
        receiptAnalysis.setStatus(event.status());

        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(OcrAnalysisStarted event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setStatus(event.status());
        receiptAnalysis.setFileIdentifier(event.fileIdentifier());
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(OcrAnalysisRegistered event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setOcrJobId(event.ocrJobId());
        receiptAnalysis.setStatus(event.status());
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(AiAnalysisRequested event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setStatus(event.status());
        receiptAnalysis.setOcrResult(event.analyzedDocument());
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(AiAnalysisCompleted event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setAiResult(event.aiResult());
        receiptAnalysis.setStatus(event.status());
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(AiAnalysisFailed event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setErrorMessage(event.message());
        receiptAnalysis.setStatus(event.status());
        repository.save(receiptAnalysis);
    }
}