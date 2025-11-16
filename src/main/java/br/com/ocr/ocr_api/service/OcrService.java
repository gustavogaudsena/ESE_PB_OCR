package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.AnalyzedDocument;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.model.AnalysisStatus;
import br.com.ocr.ocr_api.model.OcrJob;
import br.com.ocr.ocr_api.repository.OcrJobRepository;
import br.com.ocr.ocr_api.service.storage.StorageService;
import br.com.ocr.ocr_api.service.textract.OcrProcessorInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrProcessorInterface ocrProcessor;
    private final OcrJobRepository ocrJobRepository;
    private final StorageService storageService;
    @Value("${aws.config.bucket-name}")
    private String bucketName;

    public JobResponse startAnalyze(String jobId, String fileIdentifier) throws IOException {

        JobResponse job = ocrProcessor.startJob(bucketName, fileIdentifier);
        String ocrJobId = job.jobId();
        OcrJob ocrJob = new OcrJob(jobId, ocrJobId);
        ocrJobRepository.save(ocrJob);

        return job;
    }

    public OcrProcessorResponse getProcessorJobRequest(String jobId) throws IOException {

        OcrJob job = ocrJobRepository.findById(jobId)
                .orElseThrow(() -> new IOException("Job not found in database: " + jobId));

        if (job.getStatus() == AnalysisStatus.COMPLETED) {
            return new OcrProcessorResponse(job.getResult());
        }

        if (job.getStatus() == AnalysisStatus.FAILED) {
            throw new IOException("Job failed: " + job.getErrorMessage());
        }

        OcrProcessorResponse ocrProcessorResponse = ocrProcessor.getJobResult(job.getOcrJobId());

        switch (ocrProcessorResponse.getStatus()) {
            case COMPLETED:
                AnalyzedDocument analyzedDocument = ocrProcessorResponse.getDocument();
                job.setResult(analyzedDocument);
                job.setStatus(AnalysisStatus.COMPLETED);
                ocrJobRepository.save(job);
                return new OcrProcessorResponse(analyzedDocument);
            case FAILED:
                String errorMsg = ocrProcessorResponse.getErrorMessage();
                job.setStatus(AnalysisStatus.FAILED);
                job.setErrorMessage(errorMsg);
                ocrJobRepository.save(job);
                return new OcrProcessorResponse(AnalysisStatus.FAILED, errorMsg);
        }

        return new OcrProcessorResponse(AnalysisStatus.CREATED, "Job is still in progress.");
    }

    public OcrProcessorResponse fallback1(Throwable e) {
        System.out.println(e.getMessage());
        return new OcrProcessorResponse(AnalysisStatus.CREATED, "Job is still in progress.");
    }
}