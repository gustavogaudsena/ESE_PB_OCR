package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.domain.AnalyzedDocument;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.infra.ReceiptAnalysisRepository;
import br.com.ocr.ocr_api.domain.AnalysisStatus;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import br.com.ocr.ocr_api.service.textract.OcrProcessorInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrProcessorInterface ocrProcessor;
    private final ReceiptAnalysisRepository repository;
    @Value("${aws.config.bucket-name}")
    private String bucketName;

    public JobResponse startAnalyze(String jobId, String fileIdentifier) throws IOException {
        ReceiptAnalysis job = repository.findById(jobId).orElseThrow(() -> new IOException("Job not found in database"));

        return ocrProcessor.startJob(bucketName, fileIdentifier);
    }

    public OcrProcessorResponse getProcessorJobRequest(String jobId) throws IOException {

        ReceiptAnalysis job = repository.findById(jobId)
                .orElseThrow(() -> new IOException("Job not found in database"));

        if (job.getOcrResult() != null) {
            return new OcrProcessorResponse(job.getOcrResult());
        }

        if (job.getStatus() == AnalysisStatus.FAILED) {
            throw new IOException("Job failed: " + job.getErrorMessage());
        }

        OcrProcessorResponse ocrProcessorResponse = ocrProcessor.getJobResult(job.getOcrJobId());

        return switch (ocrProcessorResponse.getStatus()) {
            case COMPLETED -> {
                AnalyzedDocument analyzedDocument = ocrProcessorResponse.getDocument();
                yield new OcrProcessorResponse(analyzedDocument);
            }
            case FAILED -> {
                String errorMsg = ocrProcessorResponse.getErrorMessage();
                yield new OcrProcessorResponse(AnalysisStatus.FAILED, errorMsg);
            }
            default -> new OcrProcessorResponse(AnalysisStatus.CREATED, "Job is still in progress.");
        };
    }

    public AnalyzedDocument getAnalysis(String jobId) {
        return this.repository.findById(jobId).map(ReceiptAnalysis::getOcrResult).orElseThrow();
    }

    public ReceiptAnalysis getFullAnalysis(String jobId) {
        return this.repository.findById(jobId).orElseThrow();
    }

}