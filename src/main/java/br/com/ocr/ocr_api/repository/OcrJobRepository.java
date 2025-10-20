package br.com.ocr.ocr_api.repository;

import br.com.ocr.ocr_api.model.OcrJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OcrJobRepository extends MongoRepository<OcrJob, String> {
}
