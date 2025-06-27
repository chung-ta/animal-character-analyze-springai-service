package com.animalanalyzer.controller;

import com.animalanalyzer.model.AnalysisResponse;
import com.animalanalyzer.model.Character;
import com.animalanalyzer.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @Test
    void testAnalyzeImage() throws Exception {
        // Prepare test data
        Character mockCharacter = new Character();
        mockCharacter.setId("wise-owl");
        mockCharacter.setName("Wise Owl");
        mockCharacter.setSpecies("Great Horned Owl");
        mockCharacter.setTraits(Arrays.asList("Intelligent", "Observant", "Analytical"));
        mockCharacter.setBaseStory("You embody wisdom");
        mockCharacter.setImageUrl("/images/owl.jpg");

        AnalysisResponse mockResponse = AnalysisResponse.builder()
                .character(mockCharacter)
                .confidence(0.85)
                .reasoning("Your thoughtful expression matches the wise owl")
                .story("Like the owl, you see beyond the surface")
                .build();

        when(analysisService.analyzeImage(any())).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Perform request
        mockMvc.perform(multipart("/api/v1/analyze")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.character.name").value("Wise Owl"))
                .andExpect(jsonPath("$.character.id").value("wise-owl"))
                .andExpect(jsonPath("$.confidence").value(0.85))
                .andExpect(jsonPath("$.reasoning").exists())
                .andExpect(jsonPath("$.story").exists());
    }

    @Test
    void testAnalyzeImageEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        when(analysisService.analyzeImage(any()))
                .thenThrow(new IllegalArgumentException("File is empty"));

        mockMvc.perform(multipart("/api/v1/analyze")
                .file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAnalyzeImageServiceException() throws Exception {
        when(analysisService.analyzeImage(any()))
                .thenThrow(new RuntimeException("AI service error"));

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/analyze")
                .file(file))
                .andExpect(status().isInternalServerError());
    }
}