package br.com.ocr.ocr_api.dto;

import br.com.ocr.ocr_api.model.AnalysisStatus;
import lombok.Getter;

@Getter
public class OcrProcessorResponse {

    private final AnalysisStatus status;
    private final AnalyzedDocument document;
    private final String errorMessage;

    public OcrProcessorResponse(AnalyzedDocument document) {
        this.status = AnalysisStatus.COMPLETED;
        this.document = document;
        this.errorMessage = null;
    }

    public OcrProcessorResponse(AnalysisStatus status, String errorMessage) {
        this.status = status;
        this.document = null;
        this.errorMessage = errorMessage;
    }
}