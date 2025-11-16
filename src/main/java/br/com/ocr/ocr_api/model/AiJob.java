package br.com.ocr.ocr_api.model;

import br.com.ocr.ocr_api.dto.AiAnalyzedItem;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "aiJobs")
public class AiJob {

    @Id
    private String jobId;
    private String aiJobId;
    private String ocrJobId;
    private AnalysisStatus status;
    private List<AiAnalyzedItem> result;

    private String errorMessage;

    public AiJob(String jobId, String ocrJobId) {
        this.aiJobId = UUID.randomUUID().toString();
        this.jobId = jobId;
        this.ocrJobId = ocrJobId;
        this.status = AnalysisStatus.CREATED;
    }

    public void setCompleted() {
        this.status = AnalysisStatus.COMPLETED;
    }

    public void setFailed() {
        this.status = AnalysisStatus.FAILED;
    }
}