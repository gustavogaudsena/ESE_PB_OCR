package br.com.ocr.ocr_api.commands;

import br.com.ocr.ocr_api.domain.AnalyzedDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RequestAiAnalysis {
    @TargetAggregateIdentifier
    private String jobId;
    private String ocrJobId;
    private AnalyzedDocument analyzedDocument;
}
