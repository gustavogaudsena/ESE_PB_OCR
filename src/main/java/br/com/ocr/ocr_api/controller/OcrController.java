package br.com.ocr.ocr_api.controller;

import br.com.ocr.ocr_api.commands.RequestOcrAnalysis;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.model.AiJob;
import br.com.ocr.ocr_api.service.OcrService;
import br.com.ocr.ocr_api.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@RestController
@Slf4j
public class OcrController {
    private final OcrService ocrService;
    private final AiService aiService;
    private final CommandGateway command;

    @PostMapping
    public JobResponse startOcrAnalysis(@RequestParam("receipt") MultipartFile file) throws IOException {
        log.info("Starting recipt analysis...");
        String jobId = UUID.randomUUID().toString();
        command.send(new RequestOcrAnalysis(jobId, file.getBytes()));

        return new JobResponse(jobId);
    }

    @GetMapping("/{ocrJobId}")
    public OcrProcessorResponse getOcrAnalysis(@PathVariable String ocrJobId) throws IOException, ExecutionException, InterruptedException {
        log.info("Getting Recipt analysis OcrJobId={}", ocrJobId);
        return ocrService.getProcessorJobRequest(ocrJobId);
    }

    @PostMapping("/ai/{ocrJobId}")
    public JobResponse startAiAnalysis(@PathVariable String ocrJobId) throws IOException, ExecutionException, InterruptedException {
        log.info("AI analyses of OcrJobId={}", ocrJobId);
        return aiService.startAnalysis(ocrJobId);
    }

    @GetMapping("/ai/{jobId}")
    public AiJob getAiAnalysis(@PathVariable String jobId) {
        log.info("Getting AI Recipt analysis do JobId={}", jobId);
        return aiService.getAnalysis(jobId);
    }

}
