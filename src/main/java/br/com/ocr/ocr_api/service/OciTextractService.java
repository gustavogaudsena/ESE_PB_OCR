package br.com.ocr.ocr_api.service;

import br.com.ocr.ocr_api.dto.JobResponseDTO;
import com.oracle.bmc.aidocument.AIServiceDocumentClient;
import com.oracle.bmc.aidocument.model.*;
import com.oracle.bmc.aidocument.requests.AnalyzeDocumentRequest;
import com.oracle.bmc.aidocument.requests.CreateProcessorJobRequest;
import com.oracle.bmc.aidocument.requests.GetProcessorJobRequest;
import com.oracle.bmc.aidocument.responses.AnalyzeDocumentResponse;
import com.oracle.bmc.aidocument.responses.CreateProcessorJobResponse;
import com.oracle.bmc.aidocument.responses.GetProcessorJobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OciTextractService {

    private final AIServiceDocumentClient client;

    @Value("${oci.config.bucket-name}")
    private String namespace;
    @Value("${oci.config.bucket-name}")
    private String bucketName;
    @Value("${oci.config.compartment-id}")
    private String compartmentId;

    public JobResponseDTO createProcessorJobFromFile(MultipartFile file) throws IOException {

        byte[] bytes = file.getBytes();

        String displayName = file.getName();

        InlineDocumentContent content = InlineDocumentContent.builder()
                .data(bytes)
                .build();

        OutputLocation outputLocation = OutputLocation.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .build();

        GeneralProcessorConfig processorConfig = GeneralProcessorConfig.builder()
                .documentType(DocumentType.Receipt)
                .features(List.of(DocumentKeyValueExtractionFeature.builder().build()))
                .build();

        CreateProcessorJobDetails createProcessorJobDetails = CreateProcessorJobDetails.builder()
                .inputLocation(content)
                .outputLocation(outputLocation)
                .compartmentId(compartmentId)
                .displayName(displayName)
                .processorConfig(processorConfig)
                .build();

        CreateProcessorJobRequest createProcessorJobRequest = CreateProcessorJobRequest.builder()
                .createProcessorJobDetails(createProcessorJobDetails)
                .build();

        CreateProcessorJobResponse response = client.createProcessorJob(createProcessorJobRequest);

        ProcessorJob job = response.getProcessorJob();

        return new JobResponseDTO(job.getId());
    }

    public GetProcessorJobResponse getProcessorJobRequest(String jobId) {

        GetProcessorJobRequest getProcessorJobRequest = GetProcessorJobRequest.builder()
                .processorJobId(jobId)
                .build();

        return client.getProcessorJob(getProcessorJobRequest);
    }

    public String analyzeFromFile(MultipartFile file) throws IOException {

        byte[] bytes = file.getBytes();

        String displayName = file.getName();

        InlineDocumentDetails documentDetails = InlineDocumentDetails.builder()
                .data(bytes)
                .build();

        OutputLocation outputLocation = OutputLocation.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .build();

        AnalyzeDocumentDetails analyzeDocumentDetails = AnalyzeDocumentDetails.builder()
                .features(List.of(DocumentKeyValueExtractionFeature.builder().build()))
                .document(documentDetails)
                .compartmentId(compartmentId)
                .outputLocation(outputLocation)
                .documentType(DocumentType.Receipt)
                .build();

        AnalyzeDocumentRequest analyzeDocumentRequest = AnalyzeDocumentRequest.builder()
                .analyzeDocumentDetails(analyzeDocumentDetails)
                .build();

        AnalyzeDocumentResponse response = client.analyzeDocument(analyzeDocumentRequest);

        return response.getAnalyzeDocumentResult().toString();
    }

}