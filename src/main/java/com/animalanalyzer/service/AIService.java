package com.animalanalyzer.service;

import com.animalanalyzer.model.AIAnalysisResult;

public interface AIService {
    AIAnalysisResult analyzeImage(String imageBase64) throws Exception;
}