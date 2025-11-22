package br.com.ocr.ocr_api.events;

import br.com.ocr.ocr_api.model.AnalysisStatus;

import java.time.Instant;

public record OcrAnalysisRequested(String jobId, byte[] fileBytes, String fileIdentifier, AnalysisStatus status, Instant ts) {
    @Override
    public String toString() {
        return "OcrAnalysisRequested{jobId=" + jobId + ",ts=" + ts.toString() + "}";
    }
}
