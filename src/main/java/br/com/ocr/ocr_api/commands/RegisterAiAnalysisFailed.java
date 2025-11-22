package br.com.ocr.ocr_api.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RegisterAiAnalysisFailed {
    @TargetAggregateIdentifier
    private String jobId;
    private String ocrJobId;
    private String message;
}
