package br.com.ocr.ocr_api.model;

public enum AnalysisStatus {
    CREATED,
    FILE_UPLOADED,
    PENDING_OCR,
    PENDING_AI,
    COMPLETED,
    FAILED;

    public boolean equals(String v) {
        return v.equals(this.name());
    }
}
