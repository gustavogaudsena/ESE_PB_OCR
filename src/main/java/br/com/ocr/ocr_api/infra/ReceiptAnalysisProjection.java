package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.events.*;
import br.com.ocr.ocr_api.model.AnalysisStatus;
import br.com.ocr.ocr_api.model.ReceiptAnalysis;
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
        receiptAnalysis.setStatus(AnalysisStatus.CREATED);

        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(OcrAnalysisStarted event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setStatus(AnalysisStatus.FILE_UPLOADED);
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(OcrAnalysisRegistered event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setOcrJobId(event.ocrJobId());
        receiptAnalysis.setStatus(AnalysisStatus.PENDING_OCR);
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(AiAnalysisRequested event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setStatus(AnalysisStatus.PENDING_AI);
        receiptAnalysis.setOcrResult(event.analyzedDocument());
        repository.save(receiptAnalysis);
    }

    @EventHandler
    public void on(AiAnalysisCompleted event) {
        ReceiptAnalysis receiptAnalysis = repository.findById(event.jobId()).orElseThrow();
        receiptAnalysis.setAiResult(event.aiResult());
        receiptAnalysis.setStatus(AnalysisStatus.COMPLETED);
        repository.save(receiptAnalysis);
    }
}