package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.JobResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OCRService {

    private final OcrAnalyzerInterface ocr;

    public JobResponseDTO startAnalyzeFromFile(MultipartFile file) throws IOException {
        return ocr.startAnalyzeFromFile(file);
    }

    public Map<String, Object> getProcessorJobRequest(String jobId) throws IOException {
        return ocr.getAnalyzeByJobId(jobId);
    }

    public Map<String, Object> analyzeFromFile(MultipartFile file) throws IOException {
        return ocr.analyzeFromFile(file);
    }

}