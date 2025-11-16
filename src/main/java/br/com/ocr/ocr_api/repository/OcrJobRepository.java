package br.com.ocr.ocr_api.repository;

import br.com.ocr.ocr_api.model.OcrJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OcrJobRepository extends MongoRepository<OcrJob, String> {
    Optional<OcrJob> findByOcrJobId(String ocrJobId);
}
