package br.com.ocr.ocr_api.service.textract;

import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;

import java.io.IOException;

public interface OcrProcessorInterface {
    JobResponse startJob(String bucketName, String key);
    OcrProcessorResponse getJobResult(String jobId) throws IOException;
}
