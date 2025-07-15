# Animal Character Analyzer Spring AI Service (Backend)

A Spring Boot REST API service that analyzes user photos using Claude AI to match them with cartoon/fantasy animal characters.

## 🚀 Running the Complete Application

To run the full Animal Character Analyzer application:

```bash
# Terminal 1: Start Backend Service
cd animal-character-analyze-springai-service
export CLAUDE_API_KEY=your-claude-api-key-here
mvn spring-boot:run

# Terminal 2: Start Frontend App
cd animal-character-analyze-react-app
npm install
npm run dev
```

Then open http://localhost:5173 in your browser.

## Overview

This is the backend service for the Animal Character Analyzer project. It provides:
- RESTful API endpoints for image analysis
- Integration with Claude AI for intelligent character matching
- Stateless architecture for easy deployment
- Image processing and validation
- CORS support for web applications

## Tech Stack

- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring AI** - AI integration framework
- **Spring Web MVC** - REST API framework
- **Maven** - Build tool
- **Claude AI API** - Image analysis and character matching
- **Spring Boot Actuator** - Monitoring and health checks

## Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7+
- Claude AI API key

## Quick Start

### Prerequisites
1. Get a Claude API key from [Anthropic Console](https://console.anthropic.com/)
2. Set the API key as an environment variable:
   ```bash
   export CLAUDE_API_KEY=your-api-key-here
   ```

### Option 1: Run with Maven (Recommended)

```bash
# Clone the repository
git clone https://github.com/[your-username]/animal-character-analyze-springai-service.git
cd animal-character-analyze-springai-service

# Run the application
mvn spring-boot:run
```

The service will start on http://localhost:8080

### Option 2: Build and Run JAR

```bash
# Build the application
mvn clean package

# Run the JAR
java -jar target/animal-character-analyzer-springai-service-0.0.1-SNAPSHOT.jar
```

### Option 3: Run with Custom Configuration

1. Copy the example configuration:
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

2. Edit `application-local.yml` with your settings

3. Run with the local profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Option 2: Using Docker
```bash
# Build the Docker image
docker build -t animal-character-analyzer-springai-service .

# Run the container
docker run -p 8080:8080 \
  -e CLAUDE_API_KEY=${CLAUDE_API_KEY} \
  -e SPRING_PROFILES_ACTIVE=development \
  animal-character-analyzer-springai-service

# Or run with docker-compose from parent directory
cd ..
docker-compose up backend
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


## Monitoring

Spring Boot Actuator endpoints:
- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics export

## Docker Support

### Local Development with Docker

1. Build the Docker image:
```bash
docker build -t animal-character-analyzer-springai-service .
```

2. Run the container:
```bash
# Basic run
docker run -p 8080:8080 -e CLAUDE_API_KEY=your-key animal-character-analyzer-springai-service

# Run with all environment variables
docker run -p 8080:8080 \
  -e CLAUDE_API_KEY=${CLAUDE_API_KEY} \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SERVER_PORT=8080 \
  --name animal-analyzer-backend \
  animal-character-analyzer-springai-service
```

3. Test the container:
```bash
# Check if service is healthy
curl http://localhost:8080/api/v1/health

# View container logs
docker logs animal-analyzer-backend

# Stop the container
docker stop animal-analyzer-backend
docker rm animal-analyzer-backend
```

### Production Build for Render

To test the exact build that Render will use:
```bash
# Build with production settings
docker build -t animal-analyzer-prod .

# Run with Render-like environment
docker run -p 3000:3000 \
  -e PORT=3000 \
  -e CLAUDE_API_KEY=${CLAUDE_API_KEY} \
  -e SPRING_PROFILES_ACTIVE=production \
  animal-analyzer-prod
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

- [animal-character-analyze-react-app](https://github.com/[your-username]/animal-character-analyze-react-app) - Frontend React application

## License

This project is licensed under the MIT License.