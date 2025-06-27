package com.animalanalyzer.controller;

import com.animalanalyzer.model.Character;
import com.animalanalyzer.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/characters")
@CrossOrigin(origins = "*")
public class CharacterController {
    
    private final CharacterService characterService;
    
    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    @GetMapping
    public ResponseEntity<List<Character>> getAllCharacters() {
        return ResponseEntity.ok(characterService.getAllCharacters());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacterById(@PathVariable String id) {
        return characterService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}