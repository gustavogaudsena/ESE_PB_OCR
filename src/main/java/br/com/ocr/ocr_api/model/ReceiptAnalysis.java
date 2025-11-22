package br.com.ocr.ocr_api.model;


import br.com.ocr.ocr_api.dto.AiAnalyzedItem;
import br.com.ocr.ocr_api.dto.AnalyzedDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "receipts")
public class ReceiptAnalysis {

    @Id
    private String id;
    private String ocrJobId;
    private String aiJobId;
    private AnalysisStatus status;
    private AnalyzedDocument ocrResult;
    private List<AiAnalyzedItem> aiResult;
    private byte[] fileBytes;

    public ReceiptAnalysis() {
    }
}
