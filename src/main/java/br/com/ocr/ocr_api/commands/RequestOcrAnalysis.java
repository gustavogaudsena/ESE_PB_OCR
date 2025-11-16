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
public class RequestOcrAnalysis {
    @TargetAggregateIdentifier
    private String jobId;
    private byte[] fileBytes;
}