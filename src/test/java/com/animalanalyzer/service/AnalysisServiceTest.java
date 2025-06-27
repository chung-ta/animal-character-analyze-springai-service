package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.animalanalyzer.model.AnalysisResponse;
import com.animalanalyzer.model.Character;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AIService aiService;

    @Mock
    private CharacterService characterService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private AnalysisService analysisService;

    private Character mockCharacter;
    private AIAnalysisResult mockAIResult;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockCharacter = new Character();
        mockCharacter.setId("playful-otter");
        mockCharacter.setName("Playful Otter");
        mockCharacter.setSpecies("River Otter");
        mockCharacter.setTraits(Arrays.asList("Social", "Energetic", "Fun-loving"));
        mockCharacter.setBaseStory("You radiate positive energy");
        mockCharacter.setImageUrl("/images/otter.jpg");

        mockAIResult = AIAnalysisResult.builder()
                .suggestedCharacter("Playful Otter")
                .confidence(0.92)
                .traits(Arrays.asList("Social", "Energetic", "Fun-loving"))
                .reasoning("Your cheerful expression shows a playful nature")
                .personalizedStory("Like the otter, you bring joy to those around you")
                .build();

        mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void testAnalyzeImageSuccess() throws Exception {
        String processedImageBase64 = "processedBase64String";
        
        when(imageService.processImage(any())).thenReturn(processedImageBase64);
        when(aiService.analyzeImage(processedImageBase64)).thenReturn(mockAIResult);
        when(characterService.findByName("Playful Otter")).thenReturn(Optional.of(mockCharacter));

        AnalysisResponse response = analysisService.analyzeImage(mockFile);

        assertNotNull(response);
        assertEquals("Playful Otter", response.getCharacter().getName());
        assertEquals(0.92, response.getConfidence());
        assertEquals("Your cheerful expression shows a playful nature", response.getReasoning());
        assertEquals("Like the otter, you bring joy to those around you", response.getStory());

        verify(imageService).processImage(mockFile);
        verify(aiService).analyzeImage(processedImageBase64);
        verify(characterService).findByName("Playful Otter");
    }

    @Test
    void testAnalyzeImageCharacterNotFound() throws Exception {
        String processedImageBase64 = "processedBase64String";
        
        when(imageService.processImage(any())).thenReturn(processedImageBase64);
        when(aiService.analyzeImage(processedImageBase64)).thenReturn(mockAIResult);
        when(characterService.findByName("Playful Otter")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            analysisService.analyzeImage(mockFile);
        });

        verify(characterService).findByName("Playful Otter");
    }

    @Test
    void testAnalyzeImageAIServiceException() throws Exception {
        String processedImageBase64 = "processedBase64String";
        
        when(imageService.processImage(any())).thenReturn(processedImageBase64);
        when(aiService.analyzeImage(processedImageBase64)).thenThrow(new RuntimeException("AI service error"));

        assertThrows(RuntimeException.class, () -> {
            analysisService.analyzeImage(mockFile);
        });

        verify(aiService).analyzeImage(processedImageBase64);
    }

    @Test
    void testAnalyzeImageProcessingException() throws Exception {
        when(imageService.processImage(any())).thenThrow(new RuntimeException("Image processing error"));

        assertThrows(RuntimeException.class, () -> {
            analysisService.analyzeImage(mockFile);
        });

        verify(imageService).processImage(mockFile);
        verify(aiService, never()).analyzeImage(anyString());
    }
}