package br.com.ocr.ocr_api.commands;

import br.com.ocr.ocr_api.domain.AiAnalyzedItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RegisterAiResult {
    @TargetAggregateIdentifier
    private String jobId;
    private List<AiAnalyzedItem> aiResult;

}
