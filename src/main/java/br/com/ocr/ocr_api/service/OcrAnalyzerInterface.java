package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.JobResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface OcrAnalyzerInterface {
    public Map<String, Object>  analyzeFromFile(MultipartFile file) throws IOException;
    public Map<String, Object>  getAnalyzeByJobId(String jobId) throws IOException;
    public JobResponseDTO startAnalyzeFromFile(MultipartFile file) throws IOException;
}