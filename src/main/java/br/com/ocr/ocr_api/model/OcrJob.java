package br.com.ocr.ocr_api.model;

import br.com.ocr.ocr_api.dto.AnalyzedDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ocrJobs")
public class OcrJob {

    @Id
    private String jobId;
    private AnalysisStatus status;
    private AnalyzedDocument result;

    private String errorMessage;

    public OcrJob(String jobId) {
        this.jobId = jobId;
        this.status = AnalysisStatus.PENDING;
    }
}