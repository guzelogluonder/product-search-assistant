package com.onder.productsearchassistant.service;

import com.onder.productsearchassistant.model.response.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Profile("aws")
public class BedrockLlmService implements LlmService {

    @Value("${bedrock.region}")
    private String region;

    @Value("${bedrock.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SearchResponse parse(String query) {
        try {
            BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", 512,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", buildPrompt(query))
                    }
            );

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(model)
                    .contentType("application/json")
                    .body(software.amazon.awssdk.core.SdkBytes.fromString(
                            objectMapper.writeValueAsString(requestBody),
                            StandardCharsets.UTF_8
                    ))
                    .build();

            InvokeModelResponse response = client.invokeModel(request);

            Map<?, ?> responseBody = objectMapper.readValue(
                    response.body().asUtf8String(), Map.class
            );

            var contentList = (java.util.List<?>) responseBody.get("content");
            var firstContent = (Map<?, ?>) contentList.get(0);
            String llmOutput = (String) firstContent.get("text");

            return objectMapper.readValue(llmOutput, SearchResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Bedrock request failed: " + e.getMessage());
        }

    }
    private String buildPrompt(String query) {
        return """                                                                                                                                                                                                           
                Sen bir e-ticaret arama asistanısın.                                                                                                                                                                       
                Kullanıcının sorgusunu analiz et ve SADECE aşağıdaki JSON formatında yanıt ver, başka hiçbir şey yazma:                                                                                                    
                                                                                                                                                                                                                           
                {                                                                                                                                                                                                          
                  "category": "string veya boş string",                                                                                                                                                                    
                  "color": "string veya boş string",                                                                                                                                                                       
                  "brand": "string veya boş string",                                                                                                                                                                       
                  "model": "string veya boş string",                                                                                                                                                                       
                  "priceFrom": number veya null,                                                                                                                                                                           
                  "priceTo": number veya null,                                                                                                                                                                             
                  "currency": "TRY",                                                                                                                                                                                       
                  "size": "string veya boş string",                                                                                                                                                                        
                  "freeShipping": boolean veya null                                                                                                                                                                        
                }                                                                                                                                                                                                          
                                                                                                                                                                                                                           
                Kurallar:                                                                                                                                                                                                  
                - "X TL altı" veya "X TL'den ucuz" → priceTo: X, priceFrom: null                                                                                                                                           
                - "X TL üstü" veya "X TL'den pahalı" → priceFrom: X, priceTo: null                                                                                                                                         
                - "X-Y TL arası" → priceFrom: X, priceTo: Y                                                                                                                                                                
                - category, color gibi alanları Türkçe olarak doldur                                                                                                                                                       
                                                                                                                                                                                                                           
                Örnek:                                                                                                                                                                                                     
                Sorgu: "kırmızı elbise 500 TL altı"                                                                                                                                                                        
                Yanıt: {"category":"elbise","color":"kırmızı","brand":"","model":"","priceFrom":null,"priceTo":500,"currency":"TRY","size":"","freeShipping":null}                                                         
                                                                                                                                                                                                                           
                Kullanıcı sorgusu: "%s"                                                                                                                                                                                    
                """.formatted(query);
    }
}
