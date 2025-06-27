package com.animalanalyzer.service;

import com.animalanalyzer.model.Character;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CharacterServiceTest {

    @Autowired
    private CharacterService characterService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // CharacterService is autowired by Spring
    }

    @Test
    void testGetAllCharacters() {
        List<Character> characters = characterService.getAllCharacters();
        
        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(10, characters.size()); // Should have 10 characters from characters.json
    }

    @Test
    void testFindById() {
        Optional<Character> character = characterService.findById("wise-owl");
        
        assertTrue(character.isPresent());
        assertEquals("wise-owl", character.get().getId());
        assertEquals("Wise Owl", character.get().getName());
        assertTrue(character.get().getTraits().contains("analytical"));
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Character> character = characterService.findById("non-existent");
        
        assertFalse(character.isPresent());
    }

    @Test
    void testFindByName() {
        Optional<Character> character = characterService.findByName("Noble Lion");
        
        assertTrue(character.isPresent());
        assertEquals("noble-lion", character.get().getId());
        assertEquals("Noble Lion", character.get().getName());
    }

    @Test
    void testFindByNameCaseInsensitive() {
        Optional<Character> character = characterService.findByName("noble lion");
        
        assertTrue(character.isPresent());
        assertEquals("noble-lion", character.get().getId());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<Character> character = characterService.findByName("Non Existent Animal");
        
        assertFalse(character.isPresent());
    }

    @Test
    void testCharacterProperties() {
        Character owl = characterService.findById("wise-owl").orElseThrow();
        
        assertNotNull(owl.getTraits());
        assertFalse(owl.getTraits().isEmpty());
        assertNotNull(owl.getBaseStory());
        assertNotNull(owl.getSpecies());
    }
}