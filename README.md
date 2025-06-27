# Animal Character Analyzer Service (Backend)

A Spring Boot REST API service that analyzes user photos using Claude AI to match them with cartoon/fantasy animal characters.

## Overview

This is the backend service for the Animal Character Analyzer project. It provides:
- RESTful API endpoints for image analysis
- Integration with Claude AI for intelligent character matching
- Stateless architecture with in-memory caching
- Image processing and validation
- Rate limiting for API protection

## Tech Stack

- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Web MVC** - REST API framework
- **Maven/Gradle** - Build tool
- **Caffeine** - In-memory caching
- **Claude AI API** - Image analysis and character matching
- **Spring Boot Actuator** - Monitoring and health checks

## Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7+
- Claude AI API key

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/[your-username]/animal-character-analyze-service.git
cd animal-character-analyze-service
```

2. Create `application.yml` in `src/main/resources/`:
```yaml
server:
  port: 8080

claude:
  api:
    key: ${CLAUDE_API_KEY}
    url: https://api.anthropic.com/v1/messages

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

3. Set environment variable:
```bash
export CLAUDE_API_KEY=your-api-key-here
```

4. Build and run:
```bash
# Using Maven
./mvnw spring-boot:run

# Using Gradle
./gradlew bootRun
```

The service will be available at `http://localhost:8080`

## API Endpoints

### Health Check
```
GET /api/v1/health
```

### Analyze Image
```
POST /api/v1/analyze
Content-Type: multipart/form-data
Parameter: image (file)

Response:
{
  "character": {
    "name": "Wise Owl",
    "species": "Great Horned Owl",
    "traits": ["analytical", "observant", "thoughtful"],
    "imageUrl": "/assets/characters/wise-owl.png"
  },
  "story": "Your personalized character story...",
  "confidence": 0.85
}
```

### List All Characters
```
GET /api/v1/characters

Response:
[
  {
    "id": "wise-owl",
    "name": "Wise Owl",
    "traits": ["analytical", "observant", "thoughtful"],
    "imageUrl": "/assets/characters/wise-owl.png"
  }
]
```

## Project Structure

```
src/main/java/com/animalanalyzer/
├── controller/         # REST controllers
├── service/           # Business logic
├── model/             # Data models
├── config/            # Configuration classes
├── exception/         # Custom exceptions
└── util/              # Utility classes
```

## Configuration

### Environment Variables
- `CLAUDE_API_KEY` - Claude AI API key (required)
- `SERVER_PORT` - Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Active Spring profile

### Rate Limiting
- Default: 5 requests per minute per IP address
- Configurable in `application.yml`

## Monitoring

Spring Boot Actuator endpoints:
- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics export

## Docker Support

Build and run with Docker:
```bash
docker build -t animal-character-analyzer-service .
docker run -p 8080:8080 -e CLAUDE_API_KEY=your-key animal-character-analyzer-service
```

## Testing

Run tests:
```bash
# Maven
./mvnw test

# Gradle
./gradlew test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Related Projects

- [animal-character-analyze-app](https://github.com/[your-username]/animal-character-analyze-app) - Frontend React application

## License

This project is licensed under the MIT License.