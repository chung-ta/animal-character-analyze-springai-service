# Build stage
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy start script
COPY start.sh /app/start.sh

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user and set permissions
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -s /bin/sh -D spring && \
    chmod +x /app/start.sh && \
    chown spring:spring /app/start.sh

USER spring:spring

# Expose port - Render will override this with PORT env var
EXPOSE 8080

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

# Run the application using start script
ENTRYPOINT ["/app/start.sh"]