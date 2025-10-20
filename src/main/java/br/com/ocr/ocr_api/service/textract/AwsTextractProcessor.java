package br.com.ocr.ocr_api.service.textract;

import br.com.ocr.ocr_api.dto.*;
import br.com.ocr.ocr_api.model.AnalysisStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwsTextractProcessor implements OcrProcessorInterface {
    private final TextractClient textractClient;

    public AwsTextractProcessor() {
        this.textractClient = TextractClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public AnalyzedDocument analyzeFromFile(MultipartFile file) throws IOException {
        var document = Document.builder()
                .bytes(SdkBytes.fromByteArray(file.getBytes()))
                .build();

        AnalyzeExpenseRequest request = AnalyzeExpenseRequest.builder()
                .document(document)
                .build();

        AnalyzeExpenseResponse response = textractClient.analyzeExpense(request);

        return buildAnalyzedDocument(response.expenseDocuments());
    }

    public JobResponse startJob(String bucketName, String key) {
        S3Object s3Object = S3Object.builder()
                .bucket(bucketName)
                .name(key)
                .build();

        DocumentLocation documentLocation = DocumentLocation.builder()
                .s3Object(s3Object)
                .build();

        StartExpenseAnalysisRequest startExpenseRequest = StartExpenseAnalysisRequest
                .builder()
                .documentLocation(documentLocation)
                .build();

        StartExpenseAnalysisResponse resp = textractClient.startExpenseAnalysis(startExpenseRequest);

        return new JobResponse(resp.jobId());
    }

    public OcrProcessorResponse getJobResult(String jobId) throws IOException {
        GetExpenseAnalysisRequest request = GetExpenseAnalysisRequest.builder()
                .jobId(jobId)
                .build();

        GetExpenseAnalysisResponse response = textractClient.getExpenseAnalysis(request);

        String status = response.jobStatusAsString();

        return switch (status) {
            case "SUCCEEDED" -> {
                AnalyzedDocument analyzedDocument = buildAnalyzedDocument(response.expenseDocuments());
                yield new OcrProcessorResponse(analyzedDocument);
            }
            case "FAILED" -> {
                String errorMsg = "Textract job failed: " + response.statusMessage();
                yield new OcrProcessorResponse(AnalysisStatus.FAILED, errorMsg);
            }
            case "IN_PROGRESS" -> new OcrProcessorResponse(AnalysisStatus.PENDING, "Job is still in progress.");
            default -> throw new IOException("Unknown job status from AWSTextract: " + status);
        };
    }

    private AnalyzedDocument buildAnalyzedDocument(List<ExpenseDocument> docs) {
        AnalyzedDocument response = new AnalyzedDocument();

        if (docs == null || docs.isEmpty()) {
            return response;
        }

        ExpenseDocument doc = docs.getFirst();

        response.setSummaryFields(extractSummaryFields(doc));
        response.setLineItems(extractLineItems(doc));

        return response;
    }

    private List<DetectedField> extractSummaryFields(ExpenseDocument doc) {
        if (doc.summaryFields() == null) {
            return Collections.emptyList();
        }

        return doc.summaryFields().stream()
                .filter(field -> field.type() != null &&
                        field.valueDetection() != null &&
                        field.type().text() != null &&
                        field.valueDetection().text() != null)
                .map(field -> new DetectedField(
                        field.type().text(),
                        field.valueDetection().text()
                ))
                .collect(Collectors.toList());
    }

    private List<LineItem> extractLineItems(ExpenseDocument doc) {
        List<LineItem> lineItemsResult = new ArrayList<>();

        if (doc.lineItemGroups() == null) {
            return lineItemsResult;
        }

        for (LineItemGroup group : doc.lineItemGroups()) {
            for (LineItemFields item : group.lineItems()) {

                LineItem baseItem = getItem(item);

                if (!baseItem.getFields().isEmpty()) {
                    lineItemsResult.add(baseItem);
                }
            }
        }
        return lineItemsResult;
    }

    private LineItem getItem(LineItemFields item) {
        LineItem lineItem = new LineItem();

        if (item.lineItemExpenseFields() != null) {
            for (ExpenseField field : item.lineItemExpenseFields()) {

                if (field.type() != null && field.valueDetection() != null && field.type().text() != null) {
                    DetectedField detectedField = new DetectedField(field.type().text(), field.valueDetection().text());
                    lineItem.addField(detectedField);
                }
            }
        }
        return lineItem;
    }
}
