package br.com.ocr.ocr_api.aggregate;

import br.com.ocr.ocr_api.commands.*;
import br.com.ocr.ocr_api.events.*;
import br.com.ocr.ocr_api.model.AnalysisStatus;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.Instant;

@Aggregate
@Slf4j
public class ReceiptAnalysisAggregate {
    @AggregateIdentifier
    private String jobId;
    private String ocrJobId;
    private String aiJobId;
    private AnalysisStatus status;
    private String fileIdentifier;

    protected ReceiptAnalysisAggregate() {
    }

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void handle(RequestOcrAnalysis cmd) {
        if (cmd.getJobId() == null || cmd.getJobId().isBlank()) {
            throw new IllegalArgumentException("Job id is required");
        }

        if (cmd.getFileBytes() == null) {
            throw new IllegalArgumentException("A file bytes are required");
        }

        String fileIdentifier = "file_" + cmd.getJobId();
        apply(new OcrAnalysisRequested(cmd.getJobId(), cmd.getFileBytes(), fileIdentifier, Instant.now()));
    }

    @EventSourcingHandler
    public void on(OcrAnalysisRequested event) {
        this.jobId = event.jobId();
        this.status = AnalysisStatus.CREATED;
        this.fileIdentifier = event.fileIdentifier();
    }

    @CommandHandler
    public void handle(StartOcrAnalysis cmd) {
        if (this.status != AnalysisStatus.CREATED) {
            throw new RuntimeException("Job status should be on 'CREATED' to request OCR analysis");
        }

        apply(new OcrAnalysisStarted(this.jobId, this.fileIdentifier, Instant.now()));
    }

    @EventSourcingHandler
    public void on(OcrAnalysisStarted event) {
        this.status = AnalysisStatus.FILE_UPLOADED;
    }

    @CommandHandler
    public void handle(RegisterOcrAnalysis cmd) {
        if (this.status != AnalysisStatus.FILE_UPLOADED) {
            throw new RuntimeException("Job status should be 'FILE_UPLOADED' before start OCR analysis");
        }

        if (cmd.getOcrJobId() == null || cmd.getOcrJobId().isBlank()) {
            throw new IllegalArgumentException("OcrJobId id is required");
        }

        apply(new OcrAnalysisRegistered(this.jobId, cmd.getOcrJobId(), Instant.now()));
    }

    @EventSourcingHandler
    public void on(OcrAnalysisRegistered event) {
        log.info("Ocr analysis started, waiting queue");
        this.ocrJobId = event.ocrJobId();
        this.status = AnalysisStatus.PENDING_OCR;
    }

    @CommandHandler
    public void handle(RequestAiAnalysis cmd) {
        if (this.status != AnalysisStatus.PENDING_OCR) {
            throw new RuntimeException("Job status should be 'PENDING_OCR' before before requesting AI analysis");
        }

        apply(new AiAnalysisRequested(this.jobId, this.ocrJobId, cmd.getAnalyzedDocument(), Instant.now()));
    }

    @EventSourcingHandler
    public void on(AiAnalysisRequested event) {
        this.status = AnalysisStatus.PENDING_AI;
    }

    @CommandHandler
    public void handle(RegisterAiResult cmd) {
        if (this.status != AnalysisStatus.PENDING_AI) {
            throw new RuntimeException("Job status should be 'PENDING_AI' before before registering AI ocrResult");
        }

        if (cmd.getAiJobId() == null || cmd.getAiJobId().isBlank()) {
            throw new IllegalArgumentException("AiJobId id is required");
        }

        if (cmd.getAiResult() == null || cmd.getAiResult().isEmpty()) {
            throw new IllegalArgumentException("Ai result is required");
        }

        apply(new AiAnalysisCompleted(this.jobId, this.ocrJobId, cmd.getAiJobId(), cmd.getAiResult(), Instant.now()));
    }

    @EventSourcingHandler
    public void on(AiAnalysisCompleted event) {
        this.aiJobId = event.aiJobId();
        this.status = AnalysisStatus.COMPLETED;
    }
}
