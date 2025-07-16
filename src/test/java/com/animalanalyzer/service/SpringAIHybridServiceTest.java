package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpringAIHybridServiceTest {

    @Mock
    private AnthropicChatModel chatModel;

    @Mock
    private AnthropicApi anthropicApi;

    @Mock
    private ObjectMapper objectMapper;

    private SpringAIHybridService springAIHybridService;

    private String testImageBase64;

    @BeforeEach
    void setUp() {
        springAIHybridService = new SpringAIHybridService(chatModel, anthropicApi, objectMapper);
        testImageBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEB";
    }

    @Test
    void testAnalyzeImageWithApiDisabled() throws Exception {
        // Disable API usage
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", false);

        // Execute
        val result = springAIHybridService.analyzeImage(testImageBase64);

        // Assert demo response
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
        assertEquals(0.85, result.getConfidence());
        assertEquals(4, result.getTraits().size());
        assertTrue(result.getTraits().contains("analytical"));
        assertTrue(result.getReasoning().startsWith("Demo:"));
        assertTrue(result.getPersonalizedStory().startsWith("Demo:"));
    }

    @Test
    void testAnalyzeImageWithEmptyApiKey() throws Exception {
        // Enable API but provide empty key
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", true);
        ReflectionTestUtils.setField(springAIHybridService, "apiKey", "");

        // Execute
        val result = springAIHybridService.analyzeImage(testImageBase64);

        // Assert demo response
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
        assertTrue(result.getReasoning().startsWith("Demo:"));
    }

    @Test
    void testAnalyzeImageWithNullApiKey() throws Exception {
        // Enable API but provide null key
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", true);
        ReflectionTestUtils.setField(springAIHybridService, "apiKey", null);

        // Execute
        val result = springAIHybridService.analyzeImage(testImageBase64);

        // Assert demo response
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
    }

    @Test
    void testValidateConfidenceMethod() throws Exception {
        // Test the validateConfidence method via reflection
        val validateMethod = SpringAIHybridService.class.getDeclaredMethod("validateConfidence", Double.class);
        validateMethod.setAccessible(true);
        
        // Test with null
        val resultNull = (double) validateMethod.invoke(springAIHybridService, (Double) null);
        assertEquals(0.75, resultNull);
        
        // Test with 0.0 (should not be treated as null)
        val resultZero = (double) validateMethod.invoke(springAIHybridService, 0.0);
        assertEquals(0.0, resultZero);
        
        // Test with value > 1.0
        val resultHigh = (double) validateMethod.invoke(springAIHybridService, 1.5);
        assertEquals(1.0, resultHigh);
        
        // Test with negative value
        val resultNegative = (double) validateMethod.invoke(springAIHybridService, -0.5);
        assertEquals(0.0, resultNegative);
        
        // Test with valid value
        val resultValid = (double) validateMethod.invoke(springAIHybridService, 0.85);
        assertEquals(0.85, resultValid);
    }

    @Test
    void testExtractJsonFromResponseMethod() throws Exception {
        // Test the extractJsonFromResponse method via reflection
        val extractMethod = SpringAIHybridService.class.getDeclaredMethod("extractJsonFromResponse", String.class);
        extractMethod.setAccessible(true);
        
        // Test with valid JSON embedded in text
        val responseWithJson = """
            Here is the analysis:
            {
                "suggestedCharacter": "Noble Lion",
                "confidence": 0.95
            }
            That's the result.
            """;
        val extracted = (String) extractMethod.invoke(springAIHybridService, responseWithJson);
        assertTrue(extracted.contains("\"suggestedCharacter\": \"Noble Lion\""));
        assertTrue(extracted.contains("\"confidence\": 0.95"));
        
        // Test with no JSON
        val responseNoJson = "This response contains no JSON whatsoever";
        val defaultJson = (String) extractMethod.invoke(springAIHybridService, responseNoJson);
        assertTrue(defaultJson.contains("\"suggestedCharacter\": \"Wise Owl\""));
        assertTrue(defaultJson.contains("\"confidence\": 0.75"));
    }

    @Test
    void testCreateDefaultJsonResponseMethod() throws Exception {
        // Test the createDefaultJsonResponse method via reflection
        val createDefaultMethod = SpringAIHybridService.class.getDeclaredMethod("createDefaultJsonResponse");
        createDefaultMethod.setAccessible(true);
        
        // Execute
        val defaultJson = (String) createDefaultMethod.invoke(springAIHybridService);
        
        // Assert
        assertNotNull(defaultJson);
        assertTrue(defaultJson.contains("\"suggestedCharacter\": \"Wise Owl\""));
        assertTrue(defaultJson.contains("\"confidence\": 0.75"));
        assertTrue(defaultJson.contains("\"traits\": ["));
        assertTrue(defaultJson.contains("\"reasoning\": "));
        assertTrue(defaultJson.contains("\"personalizedStory\": "));
    }

    @Test
    void testCreateDemoResponseMethod() throws Exception {
        // Test the createDemoResponse method via reflection
        val createDemoMethod = SpringAIHybridService.class.getDeclaredMethod("createDemoResponse");
        createDemoMethod.setAccessible(true);
        
        // Execute
        val result = (AIAnalysisResult) createDemoMethod.invoke(springAIHybridService);
        
        // Assert
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
        assertEquals(0.85, result.getConfidence());
        assertEquals(4, result.getTraits().size());
        assertTrue(result.getTraits().contains("analytical"));
        assertTrue(result.getTraits().contains("observant"));
        assertTrue(result.getTraits().contains("thoughtful"));
        assertTrue(result.getTraits().contains("knowledgeable"));
        assertTrue(result.getReasoning().startsWith("Demo:"));
        assertTrue(result.getPersonalizedStory().startsWith("Demo:"));
    }

    @Test 
    void testGetDefaultPromptMethod() throws Exception {
        // Test the getDefaultPrompt method via reflection
        val getDefaultPromptMethod = SpringAIHybridService.class.getDeclaredMethod("getDefaultPrompt");
        getDefaultPromptMethod.setAccessible(true);
        
        // Execute
        val prompt = (String) getDefaultPromptMethod.invoke(springAIHybridService);
        
        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("Wise Owl"));
        assertTrue(prompt.contains("Playful Otter"));
        assertTrue(prompt.contains("Noble Lion"));
        assertTrue(prompt.contains("Curious Fox"));
        assertTrue(prompt.contains("Return JSON only"));
    }

    @Test
    void testAnalyzeImageWithExceptionFallback() throws Exception {
        // Enable API usage
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", true);
        ReflectionTestUtils.setField(springAIHybridService, "apiKey", "test-api-key");
        
        // Set model and maxTokens to trigger exception due to missing RestClient configuration
        ReflectionTestUtils.setField(springAIHybridService, "model", "test-model");
        ReflectionTestUtils.setField(springAIHybridService, "maxTokens", 1000);
        
        // Execute - this will fail due to RestClient not being properly mocked and fall back to demo
        val result = springAIHybridService.analyzeImage(testImageBase64);
        
        // Assert fallback to demo response
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
        assertTrue(result.getReasoning().startsWith("Demo:"));
    }

    @Test
    void testCleanBase64Prefix() throws Exception {
        // Test various base64 prefixes
        val pngPrefix = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB";
        val jpegPrefix = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD";
        val gifPrefix = "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEAAAAALAAAAAABAAEAAAI";
        
        // All should return demo response when API is disabled
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", false);
        
        val result1 = springAIHybridService.analyzeImage(pngPrefix);
        val result2 = springAIHybridService.analyzeImage(jpegPrefix);
        val result3 = springAIHybridService.analyzeImage(gifPrefix);
        
        // All should return the same demo response
        assertEquals("Wise Owl", result1.getSuggestedCharacter());
        assertEquals("Wise Owl", result2.getSuggestedCharacter());
        assertEquals("Wise Owl", result3.getSuggestedCharacter());
    }

    @Test
    void testLoadPromptTemplateFallback() throws Exception {
        // Test the loadPromptTemplate method via reflection
        val loadPromptMethod = SpringAIHybridService.class.getDeclaredMethod("loadPromptTemplate");
        loadPromptMethod.setAccessible(true);
        
        // Execute - may use default if file not found
        val prompt = (String) loadPromptMethod.invoke(springAIHybridService);
        
        // Assert
        assertNotNull(prompt);
        // Should contain animal names whether it's from file or default
        assertTrue(prompt.contains("Owl") || prompt.contains("animal"));
    }

    @Test
    void testServiceInitialization() {
        // Test that service initializes correctly
        val service = new SpringAIHybridService(chatModel, anthropicApi, objectMapper);
        assertNotNull(service);
        
        // Test that it can handle image analysis without configuration
        AIAnalysisResult result = null;
        try {
            result = service.analyzeImage(testImageBase64);
        } catch (Exception e) {
            fail("Service should handle exceptions gracefully");
        }
        
        assertNotNull(result);
        assertEquals("Wise Owl", result.getSuggestedCharacter());
    }

    @Test
    void testNullAndEmptyInputHandling() throws Exception {
        // API disabled
        ReflectionTestUtils.setField(springAIHybridService, "useRealApi", false);
        
        // Test with empty string
        val result1 = springAIHybridService.analyzeImage("");
        assertNotNull(result1);
        assertEquals("Wise Owl", result1.getSuggestedCharacter());
        
        // Test with whitespace
        val result2 = springAIHybridService.analyzeImage("   ");
        assertNotNull(result2);
        assertEquals("Wise Owl", result2.getSuggestedCharacter());
    }

    @Test
    void testBuilderPattern() {
        // Test that AIAnalysisResult builder works correctly
        val result = AIAnalysisResult.builder()
                .suggestedCharacter("Test Animal")
                .confidence(0.5)
                .traits(List.of("trait1", "trait2"))
                .reasoning("Test reasoning")
                .personalizedStory("Test story")
                .build();
                
        assertEquals("Test Animal", result.getSuggestedCharacter());
        assertEquals(0.5, result.getConfidence());
        assertEquals(2, result.getTraits().size());
        assertEquals("Test reasoning", result.getReasoning());
        assertEquals("Test story", result.getPersonalizedStory());
    }
}