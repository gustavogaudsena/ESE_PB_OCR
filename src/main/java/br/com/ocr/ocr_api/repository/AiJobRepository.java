package br.com.ocr.ocr_api.repository;

import br.com.ocr.ocr_api.model.AiJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AiJobRepository extends MongoRepository<AiJob, String> {
}
