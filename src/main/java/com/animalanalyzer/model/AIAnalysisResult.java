package com.animalanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    private String suggestedCharacter;
    private double confidence;
    private List<String> traits;
    private String reasoning;
    private String personalizedStory;
}