package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.AnalyzedDocument;
import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.model.AnalysisStatus;
import br.com.ocr.ocr_api.model.OcrJob;
import br.com.ocr.ocr_api.repository.OcrJobRepository;
import br.com.ocr.ocr_api.service.storage.StorageServiceInterface;
import br.com.ocr.ocr_api.service.textract.OcrProcessorInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OCRService {

    private final OcrProcessorInterface ocrProcessor;
    private final OcrJobRepository ocrJobRepository;
    private final StorageServiceInterface storageService;
    @Value("${aws.config.bucket-name}")
    private String bucketName;

    public JobResponse startAnalyzeFromFile(MultipartFile file) throws IOException {
        String key = file.getName();
        var s3Path = storageService.upload(file, key);

        JobResponse job = ocrProcessor.startJob(bucketName, key);
        OcrJob ocrJob = new OcrJob(job.jobId());
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

        OcrProcessorResponse ocrProcessorResponse = ocrProcessor.getJobResult(jobId);

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

        return new OcrProcessorResponse(AnalysisStatus.PENDING, "Job is still in progress.");
    }
}