package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.animalanalyzer.model.Character;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class ClaudeAIService implements AIService {
    
    @Value("${claude.api.key}")
    private String apiKey;
    
    @Value("${claude.api.url}")
    private String apiUrl;
    
    private final CharacterService characterService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final Random random = new Random();
    
    @Value("${claude.api.model:claude-3-opus-20240229}")
    private String model;
    
    @Value("${claude.api.max-tokens:1500}")
    private int maxTokens;
    
    @Value("${claude.api.use-real-api:false}")
    private boolean useRealApi;
    
    public ClaudeAIService(CharacterService characterService, 
                          ObjectMapper objectMapper,
                          @Value("${claude.api.key}") String apiKey,
                          @Value("${claude.api.url}") String apiUrl) {
        this.characterService = characterService;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        
        // Configure connection timeouts for better performance
        ConnectionProvider provider = ConnectionProvider.builder("claude-api")
            .maxConnections(10)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
            
        HttpClient httpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(120))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(120, TimeUnit.SECONDS)));
        
        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("x-api-key", apiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
    
    @Override
    public AIAnalysisResult analyzeImage(String imageBase64) throws Exception {
        if (useRealApi && apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here")) {
            log.info("Analyzing image with Claude AI (real API)");
            return callClaudeAPI(imageBase64);
        } else {
            log.info("Analyzing image with Claude AI (simulated)");
            // Simulate processing time
            Thread.sleep(2000);
            
            // Simulate personality analysis from image
            PersonalityAnalysis personality = analyzePersonality(imageBase64);
            
            // Match character based on personality
            Character selectedCharacter = matchCharacterToPersonality(personality);
            
            // Generate a personalized story incorporating personality
            String personalizedStory = generatePersonalizedStory(selectedCharacter, personality);
            
            return AIAnalysisResult.builder()
                .suggestedCharacter(selectedCharacter.getName())
                .confidence(personality.getConfidence())
                .traits(selectedCharacter.getTraits())
                .reasoning(personality.getReasoning())
                .personalizedStory(personalizedStory)
                .build();
        }
    }
    
    private PersonalityAnalysis analyzePersonality(String imageBase64) {
        // Simulate personality detection
        // In production, this would use Claude's actual image analysis
        
        String[] expressionTypes = {
            "warm and approachable smile",
            "thoughtful and contemplative expression",
            "confident and determined gaze",
            "playful and energetic demeanor",
            "calm and serene presence"
        };
        
        String[] energyLevels = {
            "vibrant and dynamic energy",
            "steady and grounded presence",
            "gentle and nurturing aura",
            "intense and focused determination",
            "balanced and harmonious disposition"
        };
        
        String expression = expressionTypes[random.nextInt(expressionTypes.length)];
        String energy = energyLevels[random.nextInt(energyLevels.length)];
        
        return PersonalityAnalysis.builder()
            .expression(expression)
            .energy(energy)
            .confidence(0.75 + random.nextDouble() * 0.2)
            .reasoning(String.format(
                "I notice your %s combined with %s. These visual cues suggest a personality that resonates strongly with specific animal characteristics.",
                expression, energy
            ))
            .build();
    }
    
    private Character matchCharacterToPersonality(PersonalityAnalysis personality) {
        // Smart matching based on personality analysis
        // For demo, we'll use keywords to match
        List<Character> characters = characterService.getAllCharacters();
        
        if (personality.getExpression().contains("thoughtful")) {
            return characterService.findById("wise-owl").orElse(characters.get(0));
        } else if (personality.getExpression().contains("playful")) {
            return characterService.findById("playful-otter").orElse(characters.get(1));
        } else if (personality.getExpression().contains("confident")) {
            return characterService.findById("noble-lion").orElse(characters.get(2));
        } else if (personality.getExpression().contains("warm")) {
            return characterService.findById("gentle-deer").orElse(characters.get(4));
        } else {
            // Random selection for other cases
            return characters.get(random.nextInt(characters.size()));
        }
    }
    
    private String generatePersonalizedStory(Character character, PersonalityAnalysis personality) {
        return String.format(
            "Looking at your photo, I can see your %s which reveals the essence of the %s. " +
            "Your %s mirrors the %s's %s nature, showing someone who %s. " +
            "Just as the %s %s, you radiate %s energy. " +
            "This unique combination of %s and %s makes you a true %s spirit.",
            
            personality.getExpression(),
            character.getName(),
            personality.getEnergy(),
            character.getName(),
            character.getTraits().get(0),
            getPersonalityInsight(character),
            character.getName(),
            getCharacterAction(character),
            character.getTraits().get(1),
            character.getTraits().get(0),
            character.getTraits().get(2 < character.getTraits().size() ? 2 : 1),
            character.getName()
        );
    }
    
    private String getCharacterAction(Character character) {
        Map<String, String> actions = Map.of(
            "wise-owl", "observes the world with keen insight",
            "playful-otter", "brings joy to every moment",
            "noble-lion", "leads with courage and strength",
            "curious-fox", "explores with clever ingenuity",
            "gentle-deer", "moves through life with grace",
            "mighty-dragon", "soars above limitations",
            "loyal-wolf", "protects those they care about",
            "free-eagle", "embraces boundless freedom",
            "creative-peacock", "expresses unique beauty",
            "steady-turtle", "perseveres with patience"
        );
        return actions.getOrDefault(character.getId(), "embodies their true nature");
    }
    
    private String getPersonalityInsight(Character character) {
        Map<String, String> insights = Map.of(
            "wise-owl", "values deep understanding and thoughtful analysis",
            "playful-otter", "finds happiness in connecting with others",
            "noble-lion", "naturally inspires and protects those around them",
            "curious-fox", "approaches life with clever adaptability",
            "gentle-deer", "brings peace and harmony wherever they go",
            "mighty-dragon", "pursues ambitious goals with fierce determination",
            "loyal-wolf", "builds strong bonds and values community",
            "free-eagle", "seeks freedom and new horizons",
            "creative-peacock", "expresses themselves with vibrant creativity",
            "steady-turtle", "achieves success through persistence and wisdom"
        );
        return insights.getOrDefault(character.getId(), "lives authentically");
    }
    
    // Inner class for personality analysis
    @Data
    @Builder
    private static class PersonalityAnalysis {
        private String expression;
        private String energy;
        private double confidence;
        private String reasoning;
    }
    
    private AIAnalysisResult callClaudeAPI(String imageBase64) throws Exception {
        try {
            String prompt = loadPromptTemplate();
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                    Map.of(
                        "role", "user",
                        "content", List.of(
                            Map.of("type", "text", "text", prompt),
                            Map.of("type", "image", "source", Map.of(
                                "type", "base64",
                                "media_type", "image/jpeg",
                                "data", imageBase64.replaceFirst("^data:image/[^;]+;base64,", "")
                            ))
                        )
                    )
                )
            );
            
            log.info("Sending request to Claude API with model: {}", model);
            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));
            long startTime = System.currentTimeMillis();
            
            Map<String, Object> response = webClient.post()
                .uri("/messages")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("Claude API error response: {}", errorBody);
                            return Mono.error(new RuntimeException("Claude API error: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .block();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Claude API responded in {} ms", duration);
            
            log.debug("Received response from Claude API");
            
            // Extract the assistant's response
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            if (content != null && !content.isEmpty()) {
                String textResponse = (String) content.get(0).get("text");
                
                // Claude sometimes returns incomplete JSON or with formatting issues
                // Try to extract JSON from the response
                String jsonResponse = extractJsonFromResponse(textResponse);
                
                log.debug("Extracted JSON response: {}", jsonResponse);
                
                // Parse the JSON response
                AIAnalysisResult result = objectMapper.readValue(jsonResponse, AIAnalysisResult.class);
                
                // Ensure we have the character object populated
                Character character = characterService.findByName(result.getSuggestedCharacter())
                    .orElse(characterService.getAllCharacters().get(0));
                
                return AIAnalysisResult.builder()
                    .suggestedCharacter(character.getName())
                    .confidence(result.getConfidence())
                    .traits(result.getTraits() != null ? result.getTraits() : character.getTraits())
                    .reasoning(result.getReasoning())
                    .personalizedStory(result.getPersonalizedStory())
                    .build();
            }
            
            throw new RuntimeException("No content in Claude API response");
            
        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            // Fallback to simulated response
            return analyzeImage(imageBase64);
        }
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
    
    private String extractJsonFromResponse(String response) {
        // Try to find JSON object in the response
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String jsonStr = response.substring(startIndex, endIndex + 1);
            
            // Claude sometimes returns JSON with literal \n in the structure (not in strings)
            // Remove these literal \n characters that appear outside of string values
            // First, let's clean up the JSON structure
            jsonStr = jsonStr.replaceAll(",\\s*\\\\n\\s*", ", ");
            jsonStr = jsonStr.replaceAll("\\{\\s*\\\\n\\s*", "{ ");
            jsonStr = jsonStr.replaceAll("\\s*\\\\n\\s*\\}", " }");
            jsonStr = jsonStr.replaceAll(":\\s*\\\\n\\s*", ": ");
            
            // Remove any remaining literal \n that's not inside quotes
            // This is a more targeted approach that preserves \n inside string values
            StringBuilder cleaned = new StringBuilder();
            boolean inString = false;
            boolean escape = false;
            
            for (int i = 0; i < jsonStr.length(); i++) {
                char c = jsonStr.charAt(i);
                
                if (!escape && c == '"') {
                    inString = !inString;
                }
                
                if (!inString && c == '\\' && i + 1 < jsonStr.length() && jsonStr.charAt(i + 1) == 'n') {
                    // Skip literal \n outside of strings
                    i++; // Also skip the 'n'
                    cleaned.append(' '); // Replace with space
                } else {
                    cleaned.append(c);
                }
                
                escape = !escape && c == '\\';
            }
            
            return cleaned.toString().trim();
        }
        
        // If no JSON found, try to parse the entire response
        return response;
    }
    
    private String getDefaultPrompt() {
        return """
            Analyze this person's photo and match them to one of these animal characters based on their facial features, expression, and perceived personality:
            
            Characters:
            1. Wise Owl - analytical, observant, knowledge-seeking, thoughtful
            2. Playful Otter - social, energetic, fun-loving, creative
            3. Noble Lion - confident, leadership, protective, courageous
            4. Curious Fox - clever, adaptable, mischievous, quick-witted
            5. Gentle Deer - empathetic, graceful, intuitive, peaceful
            6. Mighty Dragon - ambitious, powerful, mysterious, passionate
            7. Loyal Wolf - devoted, strategic, team-oriented, protective
            8. Free Spirit Eagle - independent, visionary, bold, freedom-loving
            9. Creative Peacock - artistic, expressive, unique, vibrant
            10. Steady Turtle - patient, wise, persistent, calm
            
            Return ONLY a valid JSON response with this exact structure (ensure all strings are properly escaped):
            {
              "suggestedCharacter": "Character name from the list above",
              "confidence": 0.0-1.0,
              "traits": ["trait1", "trait2", "trait3", "trait4"],
              "reasoning": "Your reasoning here as a single line without newlines",
              "personalizedStory": "Your story here as a single line without newlines"
            }
            """;
    }
}