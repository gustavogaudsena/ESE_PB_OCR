package br.com.ocr.ocr_api.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
public class S3StorageService implements StorageServiceInterface {

    private final S3Client s3;
    private final String bucketName = "stockflow-textract-dev-us";

    public S3StorageService() {
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder()
                .region(region)
                .build();
    }

    @Override
    public Object upload(MultipartFile file, String key) {
        try {
            byte[] bytes = file.getBytes();

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PutObjectResponse response = s3.putObject(putOb, RequestBody.fromBytes(bytes));
            System.out.printf("File uploaded successfully to %s/%s", bucketName, key);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
