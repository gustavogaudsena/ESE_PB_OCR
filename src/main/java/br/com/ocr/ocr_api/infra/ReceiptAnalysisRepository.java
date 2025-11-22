package br.com.ocr.ocr_api.infra;

import br.com.ocr.ocr_api.model.ReceiptAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReceiptAnalysisRepository extends MongoRepository<ReceiptAnalysis, String> {
    Optional<ReceiptAnalysis> findByOcrJobId(String ocrJobId);
}
