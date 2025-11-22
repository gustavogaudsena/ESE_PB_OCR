package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.commands.RegisterAiAnalysisFailed;
import br.com.ocr.ocr_api.commands.RegisterAiResult;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.infra.StockflowClient;
import br.com.ocr.ocr_api.model.AiJob;
import br.com.ocr.ocr_api.model.OcrJob;
import br.com.ocr.ocr_api.repository.AiJobRepository;
import br.com.ocr.ocr_api.repository.OcrJobRepository;
import br.com.ocr.ocr_api.service.ai.AiProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiJobRepository aiJobRepository;
    private final OcrJobRepository ocrJobRepository;
    private final AiProcessor aiProcessor;
    private final CommandGateway command;
    private final StockflowClient stockflowClient;

    public JobResponse startAnalysis(String jobId) throws IOException {
        OcrJob ocrJob = ocrJobRepository.findById(jobId).orElseThrow(() -> new IOException("Job not found in database: " + jobId));

        AiJob newJob = new AiJob(jobId, ocrJob.getOcrJobId());
        aiJobRepository.save(newJob);


        aiProcessor.analyzeItemList(ocrJob.getResult().getLineItems(), newJob.getJobId())
                .thenAccept((result) -> {
                    command.send(new RegisterAiResult(jobId, ocrJob.getOcrJobId(), result.getResult()));
                })
                .exceptionally(e -> {
                    command.send(new RegisterAiAnalysisFailed(jobId, ocrJob.getOcrJobId(), e.getMessage()));
                    log.error("AI analysis failed for jobId {}", jobId);
                    return null;
                });

        return new JobResponse(newJob.getJobId());
    }

    public AiJob getAnalysis(String jobId) {
        return aiJobRepository.findById(jobId).orElseThrow();
    }

    public void createTransaction(String jobId) throws IOException {
        AiJob aiJob = aiJobRepository.findById(jobId).orElseThrow();

        if (aiJob.getResult() == null) {
            throw new IOException("Analysis ocrResult should not be null");
        }

        stockflowClient.createTransactionByList(aiJob.getResult());
    }

}
