package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.commands.RegisterOcrFailure;
import br.com.ocr.ocr_api.commands.RequestAiAnalysis;
import br.com.ocr.ocr_api.dto.OcrProcessorResponse;
import br.com.ocr.ocr_api.domain.ReceiptAnalysis;
import br.com.ocr.ocr_api.service.OcrService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TextractSqsListener {

    private final CommandGateway command;
    private final ObjectMapper objectMapper;
    private final OcrService ocrService;
    private final ReceiptAnalysisRepository repository;

    @SqsListener("ocr-completed-queue")
    public void handleTextractNotification(String messageJson) throws JsonProcessingException {

        SnsNotification snsNotification = objectMapper.readValue(messageJson, SnsNotification.class);

        TextractStatusNotification notification = objectMapper.readValue(
                snsNotification.message(),
                TextractStatusNotification.class
        );

        String ocrJobId = notification.JobId();
        String status = notification.Status();

        log.info("SQS: Receiving Ocr Notification [{}], Status: {}", ocrJobId, status);

        String jobId = repository.findByOcrJobId(ocrJobId)
                .map(ReceiptAnalysis::getId)
                .orElse(null);

        if (jobId == null) {
            log.warn("SQS: OcrJobId [{}] Unknown.", ocrJobId);
            return;
        }

        if ("SUCCEEDED".equals(status)) {
            try {
                OcrProcessorResponse resp = ocrService.getProcessorJobRequest(jobId);
                command.send(new RequestAiAnalysis(jobId, ocrJobId, resp.getDocument()));
            } catch (Exception e) {
                log.error("SQS: Failed processing results for [{}].", ocrJobId, e);
                command.send(new RegisterOcrFailure(jobId, "Failed to retrieve OCR results: " + e.getMessage()));
            }
        } else if ("FAILED".equals(status)) {
            command.send(new RegisterOcrFailure(jobId, "OCR service provider failure"));
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextractStatusNotification(String JobId, String Status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SnsNotification(@JsonProperty("Message") String message) {
    }
}