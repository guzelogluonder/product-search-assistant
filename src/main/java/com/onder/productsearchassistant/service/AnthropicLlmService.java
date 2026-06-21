package com.onder.productsearchassistant.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.onder.productsearchassistant.model.response.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Profile("anthropic")
public class AnthropicLlmService implements LlmService {

    @Value("${anthropic.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SearchResponse parse(String query) {
        try {
            AnthropicClient client = AnthropicOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_HAIKU_4_5)
                    .maxTokens(1024L)
                    .addUserMessage(buildPrompt(query))
                    .build();

            String llmOutput = client.messages().create(params)
                    .content()
                    .stream()
                    .flatMap(block -> block.text().stream())
                    .map(t -> t.text())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Empty response from Anthropic"));

            return objectMapper.readValue(llmOutput, SearchResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Anthropic request failed: " + e.getMessage());
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
