package br.com.ocr.ocr_api.controller;

import br.com.ocr.ocr_api.commands.RequestOcrAnalysis;
import br.com.ocr.ocr_api.commands.RetryAnalysis;
import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import br.com.ocr.ocr_api.domain.AnalyzedDocument;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import br.com.ocr.ocr_api.service.OcrService;
import br.com.ocr.ocr_api.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@Slf4j
public class OcrController {
    private final OcrService ocrService;
    private final AiService aiService;
    private final CommandGateway command;

    @PostMapping
    public ResponseEntity<JobResponse> startOcrAnalysis(@RequestParam("receipt") MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws IOException {
        String jobId = UUID.randomUUID().toString();
        String userId = jwt.getClaimAsString("sub");
        command.send(new RequestOcrAnalysis(jobId, file.getBytes(), userId));

        return ResponseEntity.status(HttpStatus.CREATED).body(new JobResponse(jobId));
    }

    @PostMapping("/retry/{jobId}")
    public ResponseEntity<Void> retryAnalysis(@PathVariable String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("JobId é obrigatório");
        }
        command.sendAndWait(new RetryAnalysis(jobId));
        return ResponseEntity.accepted().build();

    }

    @GetMapping("/ocr/{jobId}")
    public ResponseEntity<AnalyzedDocument> getOcrAnalysis(@PathVariable String jobId) {
        return ResponseEntity.ok().body(ocrService.getAnalysis(jobId));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ReceiptAnalysis> getFullAnalysis(@PathVariable String jobId) {
        return ResponseEntity.ok().body(ocrService.getFullAnalysis(jobId));
    }

    @GetMapping("/ai/{jobId}")
    public ResponseEntity<List<AiAnalyzedItem>> getAiAnalysis(@PathVariable String jobId) {
        return ResponseEntity.ok().body(aiService.getAnalysis(jobId));
    }

}
