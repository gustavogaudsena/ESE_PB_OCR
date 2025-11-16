package br.com.ocr.ocr_api.service.ai;

import br.com.ocr.ocr_api.dto.AiAnalyzedItem;
import br.com.ocr.ocr_api.dto.LineItem;
import br.com.ocr.ocr_api.model.AiJob;
import br.com.ocr.ocr_api.repository.AiJobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiProcessor implements AiProcessor {

    private final Client client;
    private final AiJobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public GeminiProcessor(@Value("${gemini.config.api-key}") String apiKey, AiJobRepository jobRepository, ObjectMapper objectMapper) {
        client = Client.builder()
                .apiKey(apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
    }

    @Async
    public CompletableFuture<AiJob> analyzeItemList(List<LineItem> lineItems, String jobId) {
        try {
            Schema schema = Schema.builder()
                    .example(List.of(AiAnalyzedItem.class))
                    .build();

            String systemText = """
                                        # ROLE AND GOAL
                                        You are an expert AI for receipt and invoice data normalization.
                                        Your task is to process a JSON array of line items extracted by an OCR.
                                        You MUST return a JSON array matching the provided `responseSchema`.
                    
                                        # INPUT DATA STRUCTURE
                                        The user will provide a JSON array of `LineItem` objects.
                                        - Each `LineItem` has one key: "fields".
                                        - "fields" is an array of `DetectedField` objects.
                                        - Each `DetectedField` object has two keys: "label" and "value".
                                        - The "label" is the type of data found (e.g., "ITEM", "PRICE", "PRODUCT_CODE").
                                        - The "value" is the raw text extracted by the OCR.
                    
                                        # PROCESSING RULES
                                        For EACH `LineItem` in the user's JSON array, you MUST perform the following:
                    
                                        1.  **Strict Adherence:** Base your analysis ONLY on the data in the "fields" array for that specific item.
                    
                                        2.  **Category (AiAnalyzedItem.category):**
                                            - Analyze the "value" of the field with "label": "ITEM".
                                            - Classify it into ONE of these categories: [FOOD, CLEANING_PRODUCTS, PERSONAL_CARE, HOMEWARE, SERVICES, OTHERS].
                                            - If unsure, use "OTHERS".
                    
                                        3.  **Product Type (AiAnalyzedItem.productType):**
                                            - This is the *general class* of the product, in Portuguese (pt-BR).
                                            - Example: If `name` is "Maçã Gala" or "Maçã Fuji", the `productType` is "Maçã".
                                            - Example: If `name` is "Arroz Agulhinha Tipo 1", the `productType` is "Arroz".
                                            - Example: If `name` is "Sabão em Pó Omo", the `productType` is "Sabão em Pó".
                    
                                        3.  **Name Normalization (AiAnalyzedItem.name & .original):**
                                            - Find the field with "label": "ITEM".
                                            - The "value" (e.g., "MACA NAC GALA 540 kg") MUST be placed in the `AiAnalyzedItem.original` field.
                                            - Do your best to clean and normalize this value into a proper product name in Portuguese (pt-BR) (e.g., "Maçã Nacional Gala") and place it in the `AiAnalyzedItem.name` field.
                    
                                        4.  **Value Extraction (AiAnalyzedItem.price, .quantity, .unitType, .total):**
                                            - `unitPrice`: Find the "value" from the "label": "UNIT_PRICE" or "PRICE". Clean it into a numeric format (e.g., "7 49" -> 7.49).
                                            - `quantity`: Infer from the "ITEM" value (e.g., "540 kg") or a "QUANTITY" label. Default to 1 if not found.
                                            - `unitType`: Infer from the "ITEM" value (e.g., "kg", "g", "l", "ml", "UN"). This MUST be a standardized abbreviation. **If a specific unit (like kg, g, l, ml) is not explicitly found, you MUST default to "UN" (for "unit").**
                                            - `totalPrice`: Find the "value" from the "label": "PRICE" or "TOTAL".
                    
                                        6.  **Metadata Extraction (AiAnalyzedItem.metadata):**
                                            - This MUST be a JSON array of strings, and ALL strings MUST be in **lowercase**.
                                            - You MUST include TWO types of metadata in this list, following specific rules:
                    
                                            - **A) Implicit Metadata (Inferred Groups):**
                                                - These are the general groups you infer (e.g., "Fruta", "Legume", "Limpeza de Cozinha", "Higiene Bucal").
                                                - **Rule:** You MUST tokenize (break) multi-word groups into their individual, meaningful parts.
                                                - **Example 1:** "Limpeza de Cozinha" MUST be added as: `["limpeza", "cozinha"]`.
                                                - **Example 2:** "Higiene Bucal" MUST be added as: `["higiene", "bucal"]`.
                                                - **Example 3:** "Fruta" (single word) is just: `["fruta"]`.
                    
                                            - **B) Explicit Metadata (Extracted Attributes):**
                                                - These are specific attributes you find *directly* in the `name` or `original` fields (e.g., "Folha Dupla", "Tipo 1", "Gala", "500g").
                                                - **Rule:** You MUST keep these attributes intact as a single keyphrase. Do NOT break them.
                                                - **Example 1:** "Folha Dupla" MUST be added as: `["folha dupla"]`.
                                                - **Example 2:** "Tipo 1" MUST be added as: `["tipo 1"]`.
                                                - **Example 3:** "Gala" is just: `["gala"]`.
                                            - **Consolidated Example:** For "Papel Higiênico Folha Dupla Neve":
                                                - Inferred Group (A) = "Higiene Pessoal" -> `["higiene", "pessoal"]`
                                                - Extracted Attributes (B) = "Folha Dupla" and "Neve" -> `["folha dupla", "neve"]`
                                                - **Final `metadata` list:** `["higiene", "pessoal", "folha dupla", "neve"]`
                    
                                        # CRITICAL CONSTRAINTS
                                        - DO NOT invent or "hallucinate" items, products, or values.
                                        - If the user provides 5 items, you MUST return exactly 5 items.
                                        - Your entire response MUST be based *only* on the JSON data provided by the user.
                                        - `category` is the department (e.g., FOOD).
                                        - `productType` is the item class (e.g., Maçã).
                                        - `metadata` list MUST be in lowercase.
                                        - Inferred groups (like "Limpeza de Cozinha") ARE tokenized (broken).
                                        - Extracted attributes (like "Folha Dupla") ARE NOT tokenized (kept intact).
                    """;

            Part systemInstructionPart = Part.builder()
                    .text(systemText)
                    .build();

            Content systemInstruction = Content.builder()
                    .parts(List.of(systemInstructionPart))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .responseSchema(schema)
                    .systemInstruction(systemInstruction)
                    .build();
            String lineItemsJson = objectMapper.writeValueAsString(lineItems);

            String userPrompt = "Process the following JSON array of OCR line items according to the system instructions. " +
                    "Do not invent data. Base your response strictly on this JSON:\n\n" +
                    lineItemsJson;

            GenerateContentResponse response = client.models.generateContent(
                    GeminiModel.PRO_2_5.getModel(),
                    userPrompt,
                    config);


            String jsonResponseText = response.text();

            List<AiAnalyzedItem> aiAnalyzedList = objectMapper.readValue(
                    jsonResponseText,
                    new TypeReference<List<AiAnalyzedItem>>() {}
            );


            AiJob job = jobRepository.findById(jobId).orElseThrow();
            job.setCompleted();
            job.setResult(aiAnalyzedList);
            jobRepository.save(job);

            return CompletableFuture.completedFuture(job);
        } catch (Exception e) {
            AiJob job = jobRepository.findById(jobId).orElseThrow();
            job.setFailed();
            job.setErrorMessage(e.getMessage());
            jobRepository.save(job);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum GeminiModel {
        FLASH_2_5("gemini-2.5-flash"),
        PRO_2_5("gemini-2.5-pro");
        private final String model;
    }
}
