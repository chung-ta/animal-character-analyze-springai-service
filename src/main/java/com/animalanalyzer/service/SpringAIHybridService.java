package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Hybrid Spring AI service that uses Spring AI components
 * but sends images properly to Anthropic's API
 */
@Service
@Primary
@Slf4j
public class SpringAIHybridService implements AIService {
    
    private final AnthropicChatModel chatModel;
    private final AnthropicApi anthropicApi;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    
    @Value("${claude.api.use-real-api:false}")
    private boolean useRealApi;
    
    @Value("${spring.ai.anthropic.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.anthropic.chat.options.model:claude-3-opus-20240229}")
    private String model;
    
    @Value("${spring.ai.anthropic.chat.options.max-tokens:1500}")
    private Integer maxTokens;
    
    public SpringAIHybridService(AnthropicChatModel chatModel,
                                AnthropicApi anthropicApi,
                                ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.anthropicApi = anthropicApi;
        this.objectMapper = objectMapper;
        
        // Create RestClient using Spring AI's configuration
        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
        
        log.info("SpringAIHybridService initialized - Spring AI with image support");
    }
    
    @Override
    public AIAnalysisResult analyzeImage(String imageBase64) throws Exception {
        if (!useRealApi || apiKey == null || apiKey.isEmpty()) {
            log.warn("Claude API disabled or no API key. Set CLAUDE_USE_REAL_API=true and provide API key");
            return createDemoResponse();
        }
        
        log.info("Analyzing image with Spring AI Hybrid approach");
        
        try {
            String promptText = loadPromptTemplate();
            
            // Clean base64 data
            String cleanedBase64 = imageBase64.replaceFirst("^data:image/[^;]+;base64,", "");
            
            // Build request body that Anthropic expects
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                    Map.of(
                        "role", "user",
                        "content", List.of(
                            Map.of(
                                "type", "image",
                                "source", Map.of(
                                    "type", "base64",
                                    "media_type", "image/jpeg",
                                    "data", cleanedBase64
                                )
                            ),
                            Map.of(
                                "type", "text",
                                "text", promptText
                            )
                        )
                    )
                )
            );
            
            log.debug("Sending image request to Anthropic API");
            
            // Use RestClient to send the request with proper headers
            ResponseEntity<Map> response = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(Map.class);
            
            log.info("Received response from Anthropic");
            
            // Extract content from response
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
            String textContent = (String) content.get(0).get("text");
            
            log.debug("Extracted response content");
            
            // Parse JSON from response
            String jsonResponse = extractJsonFromResponse(textContent);
            AIAnalysisResult result = objectMapper.readValue(jsonResponse, AIAnalysisResult.class);
            
            return AIAnalysisResult.builder()
                    .suggestedCharacter(result.getSuggestedCharacter())
                    .confidence(validateConfidence(result.getConfidence()))
                    .traits(result.getTraits() != null ? result.getTraits() : List.of())
                    .reasoning(result.getReasoning())
                    .personalizedStory(result.getPersonalizedStory())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error analyzing image: {}", e.getMessage(), e);
            return createDemoResponse();
        }
    }
    
    private double validateConfidence(Double confidence) {
        if (confidence == null) return 0.75;
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    private String extractJsonFromResponse(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        
        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1).trim();
        }
        
        log.warn("No JSON found in response, using default");
        return createDefaultJsonResponse();
    }
    
    private String createDefaultJsonResponse() {
        return """
            {
                "suggestedCharacter": "Wise Owl",
                "confidence": 0.75,
                "traits": ["analytical", "observant", "thoughtful", "knowledgeable"],
                "reasoning": "Based on the analysis",
                "personalizedStory": "Your unique qualities shine through"
            }
            """;
    }
    
    private AIAnalysisResult createDemoResponse() {
        log.info("Creating demo response");
        return AIAnalysisResult.builder()
                .suggestedCharacter("Wise Owl")
                .confidence(0.85)
                .traits(List.of("analytical", "observant", "thoughtful", "knowledgeable"))
                .reasoning("Demo: Based on analytical expression, the Wise Owl represents your thoughtful nature.")
                .personalizedStory("Demo: Like the wise owl who sees in the darkness, you possess deep insight and understanding.")
                .build();
    }
    
    private String loadPromptTemplate() {
        try {
            return new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("claude-prompt-template.txt").toURI())
            ));
        } catch (Exception e) {
            log.error("Error loading prompt template", e);
            return getDefaultPrompt();
        }
    }
    
    private String getDefaultPrompt() {
        return """
            Analyze the person in this image and match them to an animal character:
            
            Wise Owl - analytical, observant, thoughtful
            Playful Otter - social, energetic, creative
            Noble Lion - confident, leadership, courageous
            Curious Fox - clever, adaptable, quick-witted
            Gentle Deer - empathetic, graceful, peaceful
            Mighty Dragon - ambitious, powerful, passionate
            Loyal Wolf - devoted, strategic, protective
            Free Eagle - independent, visionary, bold
            Creative Peacock - artistic, expressive, vibrant
            Steady Turtle - patient, wise, calm
            
            Return JSON only:
            {
              "suggestedCharacter": "Animal Name",
              "confidence": 0.75,
              "traits": ["trait1", "trait2", "trait3", "trait4"],
              "reasoning": "Based on their expression and features",
              "personalizedStory": "A personalized story about their character"
            }
            """;
    }
}