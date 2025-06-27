package com.animalanalyzer.controller;

import com.animalanalyzer.model.AnalysisResponse;
import com.animalanalyzer.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Slf4j
public class AnalysisController {
    
    private final AnalysisService analysisService;
    
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeImage(@RequestParam("image") MultipartFile file) {
        try {
            log.info("Received analysis request for file: {}", file.getOriginalFilename());
            AnalysisResponse response = analysisService.analyzeImage(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error during analysis", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}