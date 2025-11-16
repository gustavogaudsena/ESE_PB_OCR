package br.com.ocr.ocr_api.events;

import java.time.Instant;

public record OcrAnalysisRequested(String jobId, byte[] fileBytes, String fileIdentifier, Instant ts) {
    @Override
    public String toString() {
        return "OcrAnalysisRequested{jobId=" + jobId + ",ts=" + ts.toString() + "}";
    }
}
