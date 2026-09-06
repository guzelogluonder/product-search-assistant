# product-search-assistant

**GitHub:** [guzelogluonder/product-search-assistant](https://github.com/guzelogluonder/product-search-assistant)
**Açıklama:** Product Search Assistant API

## Proje Amacı

`product-search-assistant`, doğal dilde yazılmış e-ticaret arama sorgularını (Türkçe ve İngilizce) yapılandırılmış arama filtrelerine dönüştüren bir Spring Boot REST API'sidir. Kullanıcı "red dress under 500 TL" ya da "kırmızı elbise 500 TL altı" gibi serbest metin bir sorgu gönderdiğinde, uygulama bir LLM (Large Language Model) kullanarak bu metni `category`, `color`, `brand`, `model`, `priceFrom`, `priceTo`, `currency`, `size`, `freeShipping` gibi alanlardan oluşan yapılandırılmış bir JSON filtre nesnesine çevirir. Böylece bir e-ticaret sitesindeki arama kutusuna yazılan doğal dil ifadesi, doğrudan ürün filtreleme mantığında kullanılabilecek bir forma dönüştürülmüş olur.

README'deki örnek:

```
POST /api/v1/search
{"query": "red dress under 500 TL"}

→

{
  "category": "elbise",
  "color": "kırmızı",
  "priceTo": 500,
  "currency": "TRY"
}
```

## Kullanılan Teknolojiler

- **Java 21** (pom.xml'de `java.version=21`; README'de "Java 25 + Spring Boot 4.1" ifadesi de geçiyor, ancak build ayarı Java 21'i hedefliyor)
- **Spring Boot 4.1.0** (`spring-boot-starter-parent`), `spring-boot-starter-webmvc` ile REST API
- **Maven** (Maven Wrapper ile — `mvnw` / `mvnw.cmd`)
- **Lombok** — `@Data`, `@Builder`, `@RequiredArgsConstructor` gibi anotasyonlarla boilerplate azaltma
- **Jakarta Validation** (`spring-boot-starter-validation`) — request doğrulama, özel `@NoOffensiveWords` kısıtı
- **Resilience4j** (`resilience4j-spring-boot3`, AspectJ weaver ile) — LLM çağrıları için circuit breaker
- **LLM entegrasyonları (3 farklı sağlayıcı, Spring profilleriyle seçiliyor):**
  - **Ollama** (`local` profili) — yerel olarak çalışan `llama3.2` modeli, HTTP üzerinden `/api/generate` çağrısı
  - **Anthropic Claude** (`anthropic` profili) — `anthropic-java` SDK'sı (v2.34.0), `claude-haiku-4-5` modeli
  - **AWS Bedrock** (`aws` profili) — `bedrockruntime` AWS SDK'sı (v2.25.0), `anthropic.claude-3-haiku-20240307-v1:0` modeli
- Veritabanı yok — proje herhangi bir kalıcı veri deposu (DB) kullanmıyor; kategori/renk/marka listeleri kod içinde sabit (hardcoded) olarak tanımlı
- **Terraform** — AWS altyapısını (Elastic Beanstalk, IAM, Secrets Manager, Security Group) tanımlıyor
- **deploy.sh** — AWS Elastic Beanstalk'a manuel deploy scripti (Docker kullanılmıyor; JAR + Procfile ile deploy)

## Mimari ve Klasör Yapısı

Klasik katmanlı Spring Boot mimarisi kullanılıyor (controller → service → (dış LLM API'leri)). Repository/entity katmanı yok çünkü veritabanı bağımlılığı bulunmuyor.

```
src/main/java/com/onder/productsearchassistant/
├── ProductSearchAssistantApplication.java   # @SpringBootApplication giriş noktası
├── controller/
│   ├── SearchController.java                # POST /api/v1/search
│   ├── FilterController.java                # GET /api/v1/filters/*
│   ├── HealthController.java                # GET /api/v1/health
│   └── GlobalExceptionHandler.java           # @RestControllerAdvice, merkezi hata yönetimi
├── model/
│   ├── request/SearchRequest.java            # Gelen istek DTO'su
│   └── response/                             # SearchResponse, ErrorResponse, HealthResponse
├── security/
│   └── BearerTokenFilter.java                # Bearer token doğrulayan servlet Filter
├── service/
│   ├── LlmService.java                       # Ortak arayüz (interface)
│   ├── OllamaLlmService.java                 # @Profile("local")
│   ├── AnthropicLlmService.java              # @Profile("anthropic")
│   ├── BedrockLlmService.java                # @Profile("aws")
│   └── SearchService.java                    # LlmService'i çağıran iş mantığı katmanı
├── validation/
│   ├── NoOffensiveWords.java                 # Özel validation anotasyonu
│   └── NoOffensiveWordsValidator.java        # Doğrulama mantığı
└── exception/
    └── ServiceUnavailableException.java      # LLM erişilemez olduğunda fırlatılan özel exception

terraform/
├── main.tf        # AWS Elastic Beanstalk ortamı, IAM rolü/politikaları, Security Group, Secrets Manager
├── variables.tf    # app_name, region, environment, my_ip değişkenleri
└── outputs.tf      # Elastic Beanstalk ortamının URL'sini (cname) döndürür

deploy.sh           # mvnw ile JAR paketleme, S3'e yükleme, Elastic Beanstalk'a versiyon deploy etme scripti
```

`LlmService` arayüzü strateji deseni (strategy pattern) gibi kullanılmış: hangi implementasyonun (`Ollama`/`Anthropic`/`Bedrock`) aktif olacağı Spring `@Profile` mekanizmasıyla `spring.profiles.active` ayarına göre belirleniyor. `SearchController` sadece `SearchService`'e bağımlı; `SearchService` de somut LLM implementasyonunu bilmeden `LlmService` arayüzü üzerinden çalışıyor.

## Önemli Sınıflar ve Endpointler

### REST Endpoint'leri

| Method | URL | Auth | Açıklama |
|--------|-----|------|----------|
| GET | `/api/v1/health` | Hayır | Uygulama sağlık kontrolü (status + timestamp döner) |
| POST | `/api/v1/search` | Evet (Bearer token) | Doğal dil sorgusunu arama filtrelerine çevirir |
| GET | `/api/v1/filters/categories` | Hayır | Sabit kategori listesi (elbise, pantolon, ayakkabı, telefon, laptop) |
| GET | `/api/v1/filters/colors` | Hayır | Sabit renk listesi (kırmızı, mavi, siyah, beyaz, yeşil) |
| GET | `/api/v1/filters/brands` | Hayır | Sabit marka listesi (Samsung, Apple, Nike, Adidas, Zara) |

### Önemli Sınıflar

- **`SearchController`** — `POST /api/v1/search`, `@Valid @RequestBody SearchRequest` alır, `SearchService.search()`'ü çağırır.
- **`SearchService`** — İş mantığı katmanı; sadece `llmService.parse(query)` çağırıyor, ince bir katman.
- **`LlmService`** — `SearchResponse parse(String query)` metoduyla tanımlı arayüz; üç farklı implementasyonu var (`OllamaLlmService`, `AnthropicLlmService`, `BedrockLlmService`).
- **`OllamaLlmService`** — `@Profile("local")`. Ollama'nın `/api/generate` endpoint'ine HTTP isteği atıyor, `@CircuitBreaker(name="llm", fallbackMethod="fallback")` ile Resilience4j entegrasyonu var; devre açıldığında `ServiceUnavailableException` fırlatan bir `fallback` metodu tanımlı.
- **`AnthropicLlmService`** — `@Profile("anthropic")`. Anthropic Java SDK'sı ile `claude-haiku-4-5` modelini çağırıyor.
- **`BedrockLlmService`** — `@Profile("aws")`. AWS Bedrock Runtime SDK'sı ile `anthropic.claude-3-haiku-20240307-v1:0` modelini çağırıyor (`DefaultCredentialsProvider` ile kimlik doğrulama).
- **Üç servisin de kullandığı prompt aynı**: "Sen bir e-ticaret arama asistanısın..." diye başlayan, LLM'e sadece belirtilen JSON şemasında yanıt vermesini talep eden Türkçe bir prompt (fiyat aralığı kuralları — "X TL altı", "X TL üstü", "X-Y TL arası" — dahil).
- **`SearchResponse`** — `@JsonInclude(NON_NULL)` ile işaretli; kod içindeki yorumda belirtildiği gibi, kullanıcı sadece "kırmızı elbise" derse `brand`, `size` gibi alanlar null kalır ve JSON çıktısına dahil edilmez, böylece temiz bir response döner.
- **`BearerTokenFilter`** — `jakarta.servlet.Filter` implementasyonu; `/health` ve `/filters` yollarını doğrulamadan geçiriyor, diğer isteklerde `Authorization: Bearer <token>` header'ını `app.security.token` konfigürasyon değeriyle karşılaştırıyor.
- **`GlobalExceptionHandler`** — `MethodArgumentNotValidException` (400) ve `ServiceUnavailableException` (503) için merkezi hata yanıtları üretiyor.
- **`NoOffensiveWords` / `NoOffensiveWordsValidator`** — `SearchRequest.query` alanına uygulanan özel bir Jakarta Validation kısıtı; şu an sadece `"badword1"`, `"badword2"` gibi örnek/placeholder kelimeleri kontrol ediyor (gerçek bir küfür/hakaret filtresi değil, iskelet niteliğinde).

## Çalıştırma

README'ye göre:

1. **Ollama'yı kur ve modeli indir:**
   ```bash
   ollama pull llama3.2
   ollama serve
   ```
2. **Uygulamayı çalıştır:**
   ```bash
   mvn spring-boot:run
   ```
3. **Test et:**
   ```bash
   curl http://localhost:8080/api/v1/health

   curl -X POST http://localhost:8080/api/v1/search \
     -H "Authorization: Bearer my-secret-token-123" \
     -H "Content-Type: application/json" \
     -d '{"query": "red dress under 500 TL"}'

   curl http://localhost:8080/api/v1/filters/categories
   ```

Konfigürasyon `src/main/resources/application.properties` dosyasında (`local` profili varsayılan): `app.security.token`, `ollama.base-url`, `ollama.model`. Ek olarak `application-anthropic.properties` (`ANTHROPIC_API_KEY` ortam değişkeni bekliyor) ve `application-aws.properties` (`bedrock.region`, `bedrock.model`, `SECRET_TOKEN` ortam değişkeni) profil dosyaları da mevcut; hangisinin aktif olacağı `spring.profiles.active` ile seçiliyor.

Prerequisites: Java 21+, Maven, Ollama (yerel geliştirme için).

## Notlar

- README "AWS Bedrock (production) — coming soon" dese de, kod tabanında `BedrockLlmService` ve `AnthropicLlmService` implementasyonları zaten tamamlanmış durumda; sadece varsayılan aktif profil `local` (Ollama).
- Resilience4j circuit breaker ayarları (`application.properties`) oldukça agresif: `failure-rate-threshold=100`, `minimum-number-of-calls=3`, `sliding-window-size=3`, `wait-duration-in-open-state=60s` — yalnızca `OllamaLlmService` üzerinde `@CircuitBreaker` anotasyonu var; Anthropic ve Bedrock servislerinde circuit breaker uygulanmamış.
- `/api/v1/filters/*` uçlarındaki kategori/renk/marka listeleri veritabanından değil, doğrudan kod içinde sabit (`List.of(...)`) olarak tanımlı — gerçek bir ürün kataloğu entegrasyonu yok.
- `NoOffensiveWordsValidator` şu an yalnızca iki placeholder kelime (`badword1`, `badword2`) içeriyor; gerçek bir içerik moderasyon listesi değil, muhtemelen bir iskelet/örnek implementasyon.
- Güvenlik, Spring Security yerine basit bir servlet `Filter` (`BearerTokenFilter`) ile yapılmış; token karşılaştırması sabit bir string eşleşmesi (timing-attack'e karşı korumasız, `MessageDigest.isEqual` gibi bir yöntem kullanılmamış).
- `deploy.sh` script'inde satır 22'de görünen `s3://$BUCKET/$ZIPaww` ifadesi muhtemelen bir yazım hatası (`$ZIP` değişkeninin sonuna yanlışlıkla "aww" eklenmiş gibi duruyor); bu haliyle çalıştırılırsa S3 hedef anahtarı yanlış olabilir.
- Proje veritabanı kullanmadığı için repository/entity katmanı bulunmuyor; mimari, dış bir LLM API'sine ince bir servis katmanı üzerinden vekillik eden (proxy) bir yapıya sahip.
- Terraform, AWS Elastic Beanstalk üzerinde Corretto 21 çalışma zamanı, Bedrock çağrısı için IAM politikası, Secrets Manager'da API token saklama ve bir Security Group (yalnızca `var.my_ip` üzerinden 80 portuna erişim) tanımlıyor.
