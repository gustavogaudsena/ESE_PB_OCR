package br.com.ocr.ocr_api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LineItem {

    private List<DetectedField> fields;

    public void addField(DetectedField field) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }
        this.fields.add(field);
    }
}