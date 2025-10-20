package br.com.ocr.ocr_api.controller;

import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.model.AiJob;
import br.com.ocr.ocr_api.service.OCRService;
import br.com.ocr.ocr_api.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class OCRController {
    private final OCRService ocrService;
    private final AiService aiService;

    @PostMapping
    public JobResponse startOcrAnalysis(@RequestParam("receipt") MultipartFile file) {
        try {
            return ocrService.startAnalyzeFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{ocrJobId}")
    public OcrProcessorResponse getOcrAnalysis(@PathVariable String ocrJobId) throws IOException {
        return ocrService.getProcessorJobRequest(ocrJobId);
    }

    @PostMapping("/ai/{ocrJobId}")
    public JobResponse startAiAnalysis(@PathVariable String ocrJobId) throws IOException {
        OcrProcessorResponse ocrResp = ocrService.getProcessorJobRequest(ocrJobId);
        return aiService.startAnalysis(ocrJobId, ocrResp.getDocument().getLineItems());
    }

    @GetMapping("/ai/{jobId}")
    public AiJob getAiAnalysis(@PathVariable String jobId) {
        return aiService.getAnalysis(jobId);
    }

}
