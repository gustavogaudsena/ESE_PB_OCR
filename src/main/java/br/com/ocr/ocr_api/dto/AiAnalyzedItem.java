package br.com.ocr.ocr_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiAnalyzedItem {

    private String category;
    private String productType;
    private String original;
    private String name;
    private Double unitPrice;
    private String unitType;
    private Double quantity;
    private Double totalPrice;

    private List<String> metadata;

}
