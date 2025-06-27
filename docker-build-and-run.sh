#!/bin/bash
# Quick script to build and run the Docker container

echo "Building Animal Character Analyzer Service Docker image..."
echo "Using platform: linux/amd64 for compatibility..."
docker build --platform linux/amd64 -t animal-character-analyzer-service .

if [ $? -eq 0 ]; then
    echo "Build successful! Starting container..."
    
    # Stop and remove existing container if it exists
    docker stop animal-analyzer-backend 2>/dev/null
    docker rm animal-analyzer-backend 2>/dev/null
    
    # Run the container
    docker run  \
        -p 8080:8080 \
        -e CLAUDE_API_KEY=${CLAUDE_API_KEY:-"your-api-key-here"} \
        -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"development"} \
        --name animal-analyzer-backend \
        animal-character-analyzer-service
    
    echo "Container started! Waiting for service to be ready..."
    sleep 5
    
    # Test the health endpoint
    echo "Testing health endpoint..."
    curl -s http://localhost:8080/api/v1/health | jq . || echo "Service might still be starting up..."
    
    echo ""
    echo "Service is running at: http://localhost:8080"
    echo "View logs with: docker logs -f animal-analyzer-backend"
    echo "Stop with: docker stop animal-analyzer-backend"
else
    echo "Build failed!"
    exit 1
fi