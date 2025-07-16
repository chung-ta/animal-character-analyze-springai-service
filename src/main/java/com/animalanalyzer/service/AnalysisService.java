package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;
import com.animalanalyzer.model.AnalysisResponse;
import com.animalanalyzer.model.Character;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AnalysisService {
    
    private final ImageService imageService;
    private final AIService aiService;
    private final CharacterService characterService;
    
    public AnalysisService(ImageService imageService, AIService aiService, CharacterService characterService) {
        this.imageService = imageService;
        this.aiService = aiService;
        this.characterService = characterService;
    }
    
    public AnalysisResponse analyzeImage(MultipartFile file) throws Exception {
        log.info("Starting image analysis for file: {}", file.getOriginalFilename());
        
        // Process image
        val imageBase64 = imageService.processImage(file);
        log.debug("Image processed successfully");
        
        // Analyze with AI
        val aiResult = aiService.analyzeImage(imageBase64);
        log.debug("AI analysis completed: {}", aiResult.getSuggestedCharacter());
        
        // Create a dynamic character based on Claude's analysis
        val character = Character.builder()
            .id(aiResult.getSuggestedCharacter().toLowerCase().replace(" ", "-"))
            .name(aiResult.getSuggestedCharacter())
            .traits(aiResult.getTraits())
            .build();
        
        // Build response
        return AnalysisResponse.builder()
            .character(character)
            .story(aiResult.getPersonalizedStory())
            .confidence(aiResult.getConfidence())
            .reasoning(aiResult.getReasoning())
            .build();
    }
}