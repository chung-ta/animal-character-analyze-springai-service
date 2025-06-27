package com.animalanalyzer.service;

import com.animalanalyzer.model.Character;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CharacterService {
    
    private List<Character> characters;
    private final ObjectMapper objectMapper;
    
    public CharacterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void loadCharacters() {
        try {
            ClassPathResource resource = new ClassPathResource("characters.json");
            characters = objectMapper.readValue(
                resource.getInputStream(), 
                new TypeReference<List<Character>>() {}
            );
            log.info("Loaded {} characters", characters.size());
        } catch (IOException e) {
            log.error("Failed to load characters", e);
            characters = Collections.emptyList();
        }
    }
    
    public List<Character> getAllCharacters() {
        return Collections.unmodifiableList(characters);
    }
    
    public Optional<Character> findByName(String name) {
        return characters.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst();
    }
    
    public Optional<Character> findById(String id) {
        return characters.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst();
    }
}