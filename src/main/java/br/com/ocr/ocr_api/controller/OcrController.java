package br.com.ocr.ocr_api.controller;

import br.com.ocr.ocr_api.commands.RequestOcrAnalysis;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import br.com.ocr.ocr_api.service.OcrService;
import br.com.ocr.ocr_api.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
@RestController
@Slf4j
public class OcrController {
    private final OcrService ocrService;
    private final AiService aiService;
    private final CommandGateway command;

    @PostMapping
    public JobResponse startOcrAnalysis(@RequestParam("receipt") MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws IOException {
        log.info("Starting recipt analysis...");
        String jobId = UUID.randomUUID().toString();
        String userId = jwt.getClaimAsString("sub");

        command.send(new RequestOcrAnalysis(jobId, file.getBytes(), userId));

        return new JobResponse(jobId);
    }

    @PostMapping("/{jobId}")
    public void createTransactionByAiAnalysis(@PathVariable String jobId) throws IOException, ExecutionException, InterruptedException {
        log.info("Retryng AI analysis jobId: {}", jobId);
    }

    @GetMapping("/{jobId}")
    public OcrProcessorResponse getOcrAnalysis(@PathVariable String jobId) throws IOException, ExecutionException, InterruptedException {
        log.info("Getting Recipt analysis jobId={}", jobId);
        return ocrService.getProcessorJobRequest(jobId);
    }

    @PostMapping("/ai/{jobId}")
    public JobResponse startAiAnalysis(@PathVariable String jobId) throws IOException, ExecutionException, InterruptedException {
        log.info("AI analyses of OcrJobId={}", jobId);

        aiService.startAnalysis(jobId);

        return new JobResponse(jobId);
    }

    @GetMapping("/ai/{jobId}")
    public ReceiptAnalysis getAiAnalysis(@PathVariable String jobId) {
        log.info("Getting AI Recipt analysis do JobId={}", jobId);
        return aiService.getAnalysis(jobId);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "userId", jwt.getClaimAsString("sub"),
                "username", jwt.getClaimAsString("preferred_username"),
                "email", jwt.getClaimAsString("email")
        );
    }

}
