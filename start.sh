#!/bin/sh
# Start script for Render deployment

echo "Starting Animal Character Analyzer Service..."
echo "Port: ${PORT:-8080}"
echo "Profile: ${SPRING_PROFILES_ACTIVE:-default}"

# Run the application with dynamic port binding
exec java ${JAVA_OPTS} \
  -Dserver.port=${PORT:-8080} \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production} \
  -jar app.jar