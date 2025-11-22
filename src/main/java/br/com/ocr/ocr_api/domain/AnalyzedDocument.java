package br.com.ocr.ocr_api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzedDocument {

    private List<DetectedField> summaryFields;
    private List<LineItem> lineItems;

}
