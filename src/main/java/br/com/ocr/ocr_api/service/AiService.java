package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.JobResponse;
import br.com.ocr.ocr_api.dto.LineItem;
import br.com.ocr.ocr_api.model.AiJob;
import br.com.ocr.ocr_api.repository.AiJobRepository;
import br.com.ocr.ocr_api.service.ai.AiProcessorInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiJobRepository jobRepository;
    private final AiProcessorInterface processor;

    public JobResponse startAnalysis(String ocrJobId, List<LineItem> lineItems) {
        AiJob newJob = new AiJob(ocrJobId);
        jobRepository.save(newJob);

        processor.analyzeItemList(lineItems, newJob.getJobId());

        return new JobResponse(newJob.getJobId());
    }

    public AiJob getAnalysis(String jobId) {
        return jobRepository.findById(jobId).orElseThrow();
    }


}
