# Product Search Assistant

A Spring Boot REST API that converts natural language queries into structured e-commerce search filters, supporting both Turkish and English input.

## Example

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

## Tech Stack

- Java 25 + Spring Boot 4.1
- Ollama (local) — llama3.2 model
- AWS Bedrock (production) — coming soon
- Lombok, Jakarta Validation

## Prerequisites

- Java 21+
- Maven
- [Ollama](https://ollama.com) installed and running

## Getting Started

### 1. Pull the model

```bash
ollama pull llama3.2
ollama serve
```

### 2. Run the application

```bash
mvn spring-boot:run
```

## Endpoints

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/v1/health` | No | Application health check |
| POST | `/api/v1/search` | Yes | Natural language → search filters |
| GET | `/api/v1/filters/categories` | No | Available categories |
| GET | `/api/v1/filters/colors` | No | Available colors |
| GET | `/api/v1/filters/brands` | No | Available brands |

## Authentication

The `/search` endpoint requires a Bearer token:

```
Authorization: Bearer <token>
```

Token is read from `app.security.token` in `application.properties`.

## Configuration

`src/main/resources/application.properties`:

```properties
app.security.token=my-secret-token-123
spring.profiles.active=local
ollama.base-url=http://localhost:11434
ollama.model=llama3.2
```

## Testing

```bash
# Health check
curl http://localhost:8080/api/v1/health

# Search
curl -X POST http://localhost:8080/api/v1/search \
  -H "Authorization: Bearer my-secret-token-123" \
  -H "Content-Type: application/json" \
  -d '{"query": "red dress under 500 TL"}'

# Filters
curl http://localhost:8080/api/v1/filters/categories
curl http://localhost:8080/api/v1/filters/colors
curl http://localhost:8080/api/v1/filters/brands
```

## Project Structure

```
src/main/java/com/onder/productsearchassistant/
├── controller/        # REST endpoints
├── model/
│   ├── request/       # Incoming request models
│   └── response/      # Outgoing response models
├── security/          # Bearer token filter
├── service/           # LLM service layer
└── validation/        # Custom validation annotations
```
