package br.com.ocr.ocr_api.controller;

import br.com.ocr.ocr_api.dto.JobResponseDTO;
import br.com.ocr.ocr_api.service.OCRService;
import com.oracle.bmc.aidocument.responses.CreateProcessorJobResponse;
import com.oracle.bmc.aidocument.responses.GetProcessorJobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class OCRController {
    private final OCRService service;

    @PostMapping
    public JobResponseDTO createInvoiceProcessing(@RequestParam("receipt") MultipartFile file) {
        try {
            return service.startAnalyzeFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyzeFromFile(@RequestParam("receipt") MultipartFile file) {
        try {
            return service.analyzeFromFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{jobId}")
    public Map<String, Object> getProcessorJobRequest(@PathVariable String jobId) throws IOException {
        return service.getProcessorJobRequest(jobId);
    }

}
