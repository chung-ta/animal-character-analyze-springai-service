package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.animalanalyzer.model.Character;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaudeAIServiceTest {

    @Mock
    private CharacterService characterService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private RequestBodySpec requestBodySpec;
    
    @Mock
    private RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private ResponseSpec responseSpec;
    
    private ClaudeAIService claudeAIService;
    
    private String testImageBase64;
    private Character mockCharacter;
    
    @BeforeEach
    void setUp() {
        // Create service using constructor injection
        claudeAIService = new ClaudeAIService(
            characterService,
            objectMapper,
            "test-api-key",
            "https://api.anthropic.com"
        );
        
        // Use reflection to inject mocked WebClient
        ReflectionTestUtils.setField(claudeAIService, "webClient", webClient);
        
        testImageBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEB";
        
        // Setup mock character
        mockCharacter = new Character();
        mockCharacter.setId("wise-owl");
        mockCharacter.setName("Wise Owl");
        mockCharacter.setSpecies("Great Horned Owl");
        mockCharacter.setTraits(Arrays.asList("analytical", "observant", "thoughtful", "knowledgeable"));
        mockCharacter.setBaseStory("You possess the wisdom of the ages");
        mockCharacter.setImageUrl("/images/owl.jpg");
    }
    
    @Test
    void testAnalyzeImageApiDisabled() {
        // Disable API
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", false);
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Claude API key not configured"));
    }
    
    @Test
    void testAnalyzeImageNoApiKey() {
        // Enable API but no key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "");
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Claude API key not configured"));
    }
    
    @Test
    void testAnalyzeImageDefaultApiKey() {
        // Enable API but with default placeholder key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "your-api-key-here");
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Claude API key not configured"));
    }
    
    @Test
    void testAnalyzeImageApiSuccess() throws Exception {
        // Enable API with valid key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "valid-api-key");
        ReflectionTestUtils.setField(claudeAIService, "model", "claude-3-opus-20240229");
        ReflectionTestUtils.setField(claudeAIService, "maxTokens", 1500);
        
        // Mock the Claude API response
        val apiResponse = new HashMap<String, Object>();
        val content = List.of(
            Map.of("text", """
                {
                    "suggestedCharacter": "Wise Owl",
                    "confidence": 0.92,
                    "traits": ["analytical", "observant", "thoughtful", "knowledgeable"],
                    "reasoning": "Your thoughtful gaze and contemplative expression match the Wise Owl perfectly",
                    "personalizedStory": "Like the wise owl, you see through the darkness with clarity"
                }
                """)
        );
        apiResponse.put("content", content);
        
        // Setup WebClient mock chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/messages")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
        
        // Mock ObjectMapper
        val mockResult = AIAnalysisResult.builder()
                .suggestedCharacter("Wise Owl")
                .confidence(0.92)
                .traits(Arrays.asList("analytical", "observant", "thoughtful", "knowledgeable"))
                .reasoning("Your thoughtful gaze and contemplative expression match the Wise Owl perfectly")
                .personalizedStory("Like the wise owl, you see through the darkness with clarity")
                .build();
        
        when(objectMapper.readValue(anyString(), eq(AIAnalysisResult.class))).thenReturn(mockResult);
        
        // Execute
        val result = claudeAIService.analyzeImage(testImageBase64);
        
        // Assert
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
        assertEquals(0.92, result.getConfidence());
        assertEquals(4, result.getTraits().size());
        assertTrue(result.getTraits().contains("analytical"));
        assertNotNull(result.getReasoning());
        assertNotNull(result.getPersonalizedStory());
        
        // Verify API call
        verify(webClient).post();
    }
    
    @Test
    void testAnalyzeImageApiError() throws Exception {
        // Enable API with valid key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "valid-api-key");
        ReflectionTestUtils.setField(claudeAIService, "model", "claude-3-opus-20240229");
        ReflectionTestUtils.setField(claudeAIService, "maxTokens", 1500);
        
        // Setup WebClient mock to throw exception
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/messages")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Failed to analyze image with Claude API"));
        
        // Verify the expected method calls
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/messages");
        verify(requestBodySpec).bodyValue(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToMono(Map.class);
    }
    
    @Test
    void testExtractJsonFromResponseMethod() throws Exception {
        // Test the extractJsonFromResponse method via reflection
        val extractMethod = ClaudeAIService.class.getDeclaredMethod("extractJsonFromResponse", String.class);
        extractMethod.setAccessible(true);
        
        // Test with valid JSON embedded in text
        val responseWithJson = """
            Here is the analysis:
            {
                "suggestedCharacter": "Noble Lion",
                "confidence": 0.95,
                "traits": ["brave", "leader"],
                "reasoning": "Strong presence",
                "personalizedStory": "Like a lion"
            }
            That's the result.
            """;
        val extracted = (String) extractMethod.invoke(claudeAIService, responseWithJson);
        assertTrue(extracted.contains("\"suggestedCharacter\": \"Noble Lion\""));
        assertTrue(extracted.contains("\"confidence\": 0.95"));
        
        // Test with JSON containing literal \n
        val responseWithNewlines = """
            {\n
                "suggestedCharacter": "Playful Otter",\\n
                "confidence": 0.88,\\n
                "traits": ["social", "energetic"],\\n
                "reasoning": "Cheerful expression",\\n
                "personalizedStory": "Like an otter"\\n
            }
            """;
        val cleanedJson = (String) extractMethod.invoke(claudeAIService, responseWithNewlines);
        assertFalse(cleanedJson.contains("\\n"));
        assertTrue(cleanedJson.contains("\"suggestedCharacter\": \"Playful Otter\""));
        
        // Test with no JSON
        val responseNoJson = "This response contains no JSON whatsoever";
        val result = (String) extractMethod.invoke(claudeAIService, responseNoJson);
        assertEquals(responseNoJson, result);
    }
    
    @Test
    void testGetDefaultPromptMethod() throws Exception {
        // Test the getDefaultPrompt method via reflection
        val getDefaultPromptMethod = ClaudeAIService.class.getDeclaredMethod("getDefaultPrompt");
        getDefaultPromptMethod.setAccessible(true);
        
        // Execute
        val prompt = (String) getDefaultPromptMethod.invoke(claudeAIService);
        
        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Wise Owl"));
        assertTrue(prompt.contains("Playful Otter"));
        assertTrue(prompt.contains("Noble Lion"));
        assertTrue(prompt.contains("Return ONLY a valid JSON response"));
    }
    
    @Test
    void testLoadPromptTemplateMethod() throws Exception {
        // Test the loadPromptTemplate method via reflection
        val loadPromptMethod = ClaudeAIService.class.getDeclaredMethod("loadPromptTemplate");
        loadPromptMethod.setAccessible(true);
        
        // Execute - will use default if file not found
        val prompt = (String) loadPromptMethod.invoke(claudeAIService);
        
        // Assert
        assertNotNull(prompt);
        // Should contain animal names whether from file or default
        assertTrue(prompt.contains("Owl") || prompt.contains("animal"));
    }
    
    @Test
    void testGetCharacterActionMethod() throws Exception {
        // Test the getCharacterAction method via reflection
        val getActionMethod = ClaudeAIService.class.getDeclaredMethod("getCharacterAction", Character.class);
        getActionMethod.setAccessible(true);
        
        // Test with known character
        val action = (String) getActionMethod.invoke(claudeAIService, mockCharacter);
        assertEquals("observes the world with keen insight", action);
        
        // Test with unknown character
        val unknownCharacter = new Character();
        unknownCharacter.setId("unknown-animal");
        val defaultAction = (String) getActionMethod.invoke(claudeAIService, unknownCharacter);
        assertEquals("embodies their true nature", defaultAction);
    }
    
    @Test
    void testGetPersonalityInsightMethod() throws Exception {
        // Test the getPersonalityInsight method via reflection
        val getInsightMethod = ClaudeAIService.class.getDeclaredMethod("getPersonalityInsight", Character.class);
        getInsightMethod.setAccessible(true);
        
        // Test with known character
        val insight = (String) getInsightMethod.invoke(claudeAIService, mockCharacter);
        assertEquals("values deep understanding and thoughtful analysis", insight);
        
        // Test with unknown character
        val unknownCharacter = new Character();
        unknownCharacter.setId("unknown-animal");
        val defaultInsight = (String) getInsightMethod.invoke(claudeAIService, unknownCharacter);
        assertEquals("lives authentically", defaultInsight);
    }
    
    @Test
    void testMatchCharacterToPersonalityMethod() throws Exception {
        // Setup characters
        val characters = Arrays.asList(
            mockCharacter,
            createCharacter("playful-otter", "Playful Otter"),
            createCharacter("noble-lion", "Noble Lion"),
            createCharacter("curious-fox", "Curious Fox"),
            createCharacter("gentle-deer", "Gentle Deer")
        );
        
        when(characterService.getAllCharacters()).thenReturn(characters);
        when(characterService.findById("wise-owl")).thenReturn(Optional.of(mockCharacter));
        when(characterService.findById("playful-otter")).thenReturn(Optional.of(characters.get(1)));
        when(characterService.findById("noble-lion")).thenReturn(Optional.of(characters.get(2)));
        when(characterService.findById("gentle-deer")).thenReturn(Optional.of(characters.get(4)));
        
        // Get method via reflection
        val matchMethod = ClaudeAIService.class.getDeclaredMethod("matchCharacterToPersonality", 
            Class.forName("com.animalanalyzer.service.ClaudeAIService$PersonalityAnalysis"));
        matchMethod.setAccessible(true);
        
        // Create PersonalityAnalysis instances
        val personalityClass = Class.forName("com.animalanalyzer.service.ClaudeAIService$PersonalityAnalysis");
        val builderMethod = personalityClass.getMethod("builder");
        val builder = builderMethod.invoke(null);
        
        // Test thoughtful expression
        val thoughtfulPersonality = createPersonalityAnalysis(builder, personalityClass, 
            "thoughtful and contemplative expression", "steady energy", 0.85, "Test reasoning");
        val result1 = (Character) matchMethod.invoke(claudeAIService, thoughtfulPersonality);
        assertEquals("Wise Owl", result1.getName());
        
        // Test playful expression
        val playfulPersonality = createPersonalityAnalysis(builder, personalityClass,
            "playful and energetic demeanor", "vibrant energy", 0.90, "Test reasoning");
        val result2 = (Character) matchMethod.invoke(claudeAIService, playfulPersonality);
        assertEquals("Playful Otter", result2.getName());
        
        // Test confident expression
        val confidentPersonality = createPersonalityAnalysis(builder, personalityClass,
            "confident and determined gaze", "intense energy", 0.88, "Test reasoning");
        val result3 = (Character) matchMethod.invoke(claudeAIService, confidentPersonality);
        assertEquals("Noble Lion", result3.getName());
        
        // Test warm expression
        val warmPersonality = createPersonalityAnalysis(builder, personalityClass,
            "warm and approachable smile", "gentle energy", 0.82, "Test reasoning");
        val result4 = (Character) matchMethod.invoke(claudeAIService, warmPersonality);
        assertEquals("Gentle Deer", result4.getName());
    }
    
    @Test
    void testAnalyzePersonalityMethod() throws Exception {
        // Test the analyzePersonality method via reflection
        val analyzeMethod = ClaudeAIService.class.getDeclaredMethod("analyzePersonality", String.class);
        analyzeMethod.setAccessible(true);
        
        // Execute multiple times to test randomness
        val expressions = new HashSet<String>();
        val energies = new HashSet<String>();
        
        for (var i = 0; i < 10; i++) {
            val personality = analyzeMethod.invoke(claudeAIService, testImageBase64);
            
            // Get values via reflection
            val personalityClass = personality.getClass();
            val getExpression = personalityClass.getMethod("getExpression");
            val getEnergy = personalityClass.getMethod("getEnergy");
            val getConfidence = personalityClass.getMethod("getConfidence");
            val getReasoning = personalityClass.getMethod("getReasoning");
            
            val expression = (String) getExpression.invoke(personality);
            val energy = (String) getEnergy.invoke(personality);
            val confidence = (double) getConfidence.invoke(personality);
            val reasoning = (String) getReasoning.invoke(personality);
            
            expressions.add(expression);
            energies.add(energy);
            
            // Assert valid values
            assertNotNull(expression);
            assertNotNull(energy);
            assertTrue(confidence >= 0.75 && confidence <= 0.95);
            assertTrue(reasoning.contains("I notice your"));
            assertTrue(reasoning.contains("combined with"));
        }
        
        // Should have some variety due to randomness
        assertTrue(expressions.size() > 1);
        assertTrue(energies.size() > 1);
    }
    
    @Test
    void testGeneratePersonalizedStoryMethod() throws Exception {
        // Get method via reflection
        val generateStoryMethod = ClaudeAIService.class.getDeclaredMethod("generatePersonalizedStory", 
            Character.class, Class.forName("com.animalanalyzer.service.ClaudeAIService$PersonalityAnalysis"));
        generateStoryMethod.setAccessible(true);
        
        // Create PersonalityAnalysis
        val personalityClass = Class.forName("com.animalanalyzer.service.ClaudeAIService$PersonalityAnalysis");
        val builderMethod = personalityClass.getMethod("builder");
        val builder = builderMethod.invoke(null);
        val personality = createPersonalityAnalysis(builder, personalityClass,
            "thoughtful expression", "calm energy", 0.85, "Test reasoning");
        
        // Execute
        val story = (String) generateStoryMethod.invoke(claudeAIService, mockCharacter, personality);
        
        // Assert
        assertNotNull(story);
        assertTrue(story.contains("Looking at your photo"));
        assertTrue(story.contains("Wise Owl"));
        assertTrue(story.contains("thoughtful expression"));
        assertTrue(story.contains("calm energy"));
        assertTrue(story.contains("analytical"));
    }
    
    @Test
    void testEmptyApiResponse() throws Exception {
        // Enable API with valid key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "valid-api-key");
        ReflectionTestUtils.setField(claudeAIService, "model", "claude-3-opus-20240229");
        ReflectionTestUtils.setField(claudeAIService, "maxTokens", 1500);
        
        // Mock empty API response
        val apiResponse = new HashMap<String, Object>();
        apiResponse.put("content", Collections.emptyList());
        
        // Setup WebClient mock chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/messages")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Failed to analyze image with Claude API"));
    }
    
    @Test
    void testNullContentInApiResponse() throws Exception {
        // Enable API with valid key
        ReflectionTestUtils.setField(claudeAIService, "useRealApi", true);
        ReflectionTestUtils.setField(claudeAIService, "apiKey", "valid-api-key");
        ReflectionTestUtils.setField(claudeAIService, "model", "claude-3-opus-20240229");
        ReflectionTestUtils.setField(claudeAIService, "maxTokens", 1500);
        
        // Mock API response with null content
        val apiResponse = new HashMap<String, Object>();
        apiResponse.put("content", null);
        
        // Setup WebClient mock chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/messages")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
        
        // Execute and expect exception
        val exception = assertThrows(RuntimeException.class, () -> {
            claudeAIService.analyzeImage(testImageBase64);
        });
        
        assertTrue(exception.getMessage().contains("Failed to analyze image with Claude API"));
    }
    
    // Helper methods
    private Character createCharacter(String id, String name) {
        val character = new Character();
        character.setId(id);
        character.setName(name);
        character.setTraits(Arrays.asList("trait1", "trait2", "trait3"));
        return character;
    }
    
    private Object createPersonalityAnalysis(Object builder, Class<?> personalityClass, 
                                           String expression, String energy, double confidence, String reasoning) throws Exception {
        val expressionMethod = builder.getClass().getMethod("expression", String.class);
        val energyMethod = builder.getClass().getMethod("energy", String.class);
        val confidenceMethod = builder.getClass().getMethod("confidence", double.class);
        val reasoningMethod = builder.getClass().getMethod("reasoning", String.class);
        val buildMethod = builder.getClass().getMethod("build");
        
        expressionMethod.invoke(builder, expression);
        energyMethod.invoke(builder, energy);
        confidenceMethod.invoke(builder, confidence);
        reasoningMethod.invoke(builder, reasoning);
        
        return buildMethod.invoke(builder);
    }
}