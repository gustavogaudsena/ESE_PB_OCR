package br.com.ocr.ocr_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcrResultDTO(
        String jobId,
        String status,
        Map<String, Object> extractedData
) {}