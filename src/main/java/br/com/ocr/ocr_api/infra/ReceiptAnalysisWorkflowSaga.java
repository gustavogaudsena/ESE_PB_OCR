package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.commands.RegisterOcrAnalysis;
import br.com.ocr.ocr_api.commands.StartOcrAnalysis;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.events.*;
import br.com.ocr.ocr_api.service.AiService;
import br.com.ocr.ocr_api.service.OcrService;
import br.com.ocr.ocr_api.service.storage.StorageService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.IOException;

@Saga
@Slf4j
@NoArgsConstructor
public class ReceiptAnalysisWorkflowSaga {
    @Autowired
    private OcrService ocrService;
    @Autowired
    private AiService aiService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private transient CommandGateway command;

    @Autowired
    private transient StreamBridge streamBridge;

    @StartSaga
    @SagaEventHandler(associationProperty = "jobId")
    public void on(OcrAnalysisRequested event) {
        log.info("Requesting analysis for {}", event.jobId());
        storageService.upload(event.fileBytes(), event.fileIdentifier());

        command.send(new StartOcrAnalysis(event.jobId(), event.fileIdentifier()));
    }

    @SagaEventHandler(associationProperty = "jobId")
    public void on(OcrAnalysisStarted event) throws IOException {
        log.info("Starting OCR analyze for {}", event.jobId());

        JobResponse ocrJob = ocrService.startAnalyze(event.jobId(), event.fileIdentifier());
        command.send(new RegisterOcrAnalysis(event.jobId(), ocrJob.jobId()));
    }

    @SagaEventHandler(associationProperty = "jobId")
    public void on(AiAnalysisRequested event) throws IOException {
        log.info("Ocr analysisCompleted for {}", event.jobId());
        log.info("Starting AI analysis");

        aiService.startAnalysis(event.jobId(), event.analyzedDocument());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "jobId")
    public void on(AiAnalysisCompleted event) throws IOException {
        log.info("AI analysis completed for job {}", event.jobId());
        log.info("Creating transaction based on Ai Analysis for job {}", event.jobId());

        streamBridge.send( "aiAnalysisOutput-out-0", event);
    }

}