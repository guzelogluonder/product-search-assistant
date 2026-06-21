package com.onder.productsearchassistant.service;

import com.onder.productsearchassistant.exception.ServiceUnavailableException;
import com.onder.productsearchassistant.model.response.SearchResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@Profile("local")
public class OllamaLlmService implements LlmService{

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    @Override
    public SearchResponse parse(String query) {
        try{
            String prompt = builPrompt(query);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            String llmOutput = (String) responseMap.get("response");

            return objectMapper.readValue(llmOutput, SearchResponse.class);

        }catch (Exception e){
            throw new RuntimeException("Ollama request failed: " + e.getMessage());
        }
    }

    public SearchResponse fallback(String query, Throwable t) {
        throw new ServiceUnavailableException("LLM service is temporarily unavailable");
    }

    private String builPrompt(String query){
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
