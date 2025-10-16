package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.JobResponseDTO;
import br.com.ocr.ocr_api.service.storage.S3StorageService;
import br.com.ocr.ocr_api.service.storage.StorageServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AwsTextractService implements OcrAnalyzerInterface {

    private final TextractClient textractClient;
    private final StorageServiceInterface storageService;

    @Value("${aws.config.bucket-name}")
    private String bucketName;

    public AwsTextractService() {
        this.textractClient = TextractClient.builder()
                .region(Region.US_EAST_1)
                .build();

        storageService = new S3StorageService();
    }

    public Map<String, Object> analyzeFromFile(MultipartFile file) throws IOException {

        var document = Document.builder()
                .bytes(SdkBytes.fromByteArray(file.getBytes()))
                .build();

        AnalyzeExpenseRequest request = AnalyzeExpenseRequest.builder()
                .document(document)
                .build();

        AnalyzeExpenseResponse response = textractClient.analyzeExpense(request);

        return buildExpenseAnalyzeResponse(response.expenseDocuments());
    }

    public JobResponseDTO startAnalyzeFromFile(MultipartFile file) throws IOException {
        String key = file.getName();

        var response = storageService.upload(file, key);

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

        return new JobResponseDTO(resp.jobId());
    }

    public Map<String, Object> getAnalyzeByJobId(String jobId) throws IOException {
        GetExpenseAnalysisRequest request = GetExpenseAnalysisRequest.builder()
                .jobId(jobId)
                .build();

        GetExpenseAnalysisResponse response = textractClient.getExpenseAnalysis(request);

        return buildExpenseAnalyzeResponse(response.expenseDocuments());
    }

    private Map<String, Object> buildExpenseAnalyzeResponse(List<ExpenseDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return new HashMap<>();
        }

        ExpenseDocument doc = docs.get(0);

        Map<String, Object> result = new HashMap<>();

        result.put("summary", extractSummaryFields(doc));
        result.put("lineItems", extractLineItems(doc));

        return result;
    }

    private Map<String, String> extractSummaryFields(ExpenseDocument doc) {
        return doc.summaryFields().stream()
                .filter(field -> field.labelDetection() != null && field.valueDetection() != null && field.labelDetection().text() != null)
                .collect(Collectors.toMap(
                        field -> field.labelDetection().text(),
                        field -> field.valueDetection().text(),
                        (val1, val2) -> val1
                ));
    }

    private List<Map<String, String>> extractLineItems(ExpenseDocument doc) {
        List<Map<String, String>> lineItemsResult = new ArrayList<>();

        if (doc.lineItemGroups() == null) {
            return lineItemsResult;
        }

        for (LineItemGroup group : doc.lineItemGroups()) {
            for (LineItemFields item : group.lineItems()) {
                Map<String, String> lineItemMap = new HashMap<>();
                if (item.lineItemExpenseFields() != null) {
                    for (ExpenseField field : item.lineItemExpenseFields()) {
                        if (field.type() != null && field.valueDetection() != null && field.type().text() != null) {
                            lineItemMap.put(field.type().text(), field.valueDetection().text());
                        }
                    }
                }
                if (!lineItemMap.isEmpty()) {
                    lineItemsResult.add(lineItemMap);
                }
            }
        }
        return lineItemsResult;
    }
}
