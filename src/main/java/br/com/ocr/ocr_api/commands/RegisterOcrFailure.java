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
public class RegisterOcrFailure {
    @TargetAggregateIdentifier
    private String jobId;
    private String message;
}
