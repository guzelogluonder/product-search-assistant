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

## Running with Docker

The project ships with a multi-stage `Dockerfile` and a `docker-compose.yml` that runs the API
alongside a local Ollama instance, wired together over a container network.

**Prerequisites:** Docker Desktop (or Docker Engine) with Compose v2.

### 1. Start the stack

```bash
docker compose up -d --build
```

This builds the app image, starts Ollama, and — thanks to
`depends_on: condition: service_healthy` — only starts the app once Ollama's healthcheck passes.

### 2. Pull the model (first run only)

```bash
docker exec -it product-search-assistant-ollama-1 ollama pull llama3.2
```

The model is cached in the `ollama-data` named volume, so this step isn't needed again unless the
volume is removed.

### 3. Verify

```bash
curl http://localhost:8080/api/v1/health

curl -X POST http://localhost:8080/api/v1/search \
  -H "Authorization: Bearer my-secret-token-123" \
  -H "Content-Type: application/json" \
  -d '{"query": "red dress under 500 TL"}'
```

### Configuration via environment variables

| Variable | Overrides | Default |
|---|---|---|
| `OLLAMA_BASE_URL` | `ollama.base-url` | `http://localhost:11434` |
| `OLLAMA_MODEL` | `ollama.model` | `llama3.2` |
| `APP_SECURITY_TOKEN` | `app.security.token` | `my-secret-token-123` |

Inside `docker-compose.yml`, the app reaches Ollama at `http://ollama:11434` — the compose service
name, not `localhost`, since `localhost` inside a container refers to the container itself. Put
`APP_SECURITY_TOKEN` in a git-ignored `.env` file rather than committing a real token.

### Stopping

```bash
docker compose down       # stop containers, keep the pulled model
docker compose down -v    # also remove the ollama-data volume
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
