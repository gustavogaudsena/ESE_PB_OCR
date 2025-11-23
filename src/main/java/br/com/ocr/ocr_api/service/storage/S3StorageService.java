package br.com.ocr.ocr_api.service.storage;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


@Service
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final String bucketName = "stockflow-textract-dev-us";

    public S3StorageService() {
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder()
                .region(region)
                .build();
    }

    @Override
    @CircuitBreaker(name="external")
    public Object upload(byte[] fileBytes, String key) {
        log.info("Uploading file: {}", key);

        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectResponse response = s3.putObject(putOb, RequestBody.fromBytes(fileBytes));
        System.out.printf("File uploaded successfully to %s/%s", bucketName, key);
        return response;
    }

    @Override
    public Object read() {
        return null;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
