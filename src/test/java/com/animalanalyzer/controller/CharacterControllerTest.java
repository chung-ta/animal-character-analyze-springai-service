package com.animalanalyzer.controller;

import com.animalanalyzer.model.Character;
import com.animalanalyzer.service.CharacterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(CharacterController.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CharacterService characterService;

    @Test
    void testGetAllCharacters() throws Exception {
        Character owl = new Character();
        owl.setId("wise-owl");
        owl.setName("Wise Owl");
        owl.setSpecies("Great Horned Owl");
        owl.setTraits(Arrays.asList("Intelligent", "Observant"));

        Character otter = new Character();
        otter.setId("playful-otter");
        otter.setName("Playful Otter");
        otter.setSpecies("River Otter");
        otter.setTraits(Arrays.asList("Social", "Energetic"));

        when(characterService.getAllCharacters()).thenReturn(Arrays.asList(owl, otter));

        mockMvc.perform(get("/api/v1/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("wise-owl"))
                .andExpect(jsonPath("$[0].name").value("Wise Owl"))
                .andExpect(jsonPath("$[1].id").value("playful-otter"))
                .andExpect(jsonPath("$[1].name").value("Playful Otter"));
    }

    @Test
    void testGetCharacterById() throws Exception {
        Character owl = new Character();
        owl.setId("wise-owl");
        owl.setName("Wise Owl");
        owl.setSpecies("Great Horned Owl");
        owl.setTraits(Arrays.asList("Intelligent", "Observant", "Analytical"));
        owl.setBaseStory("You embody wisdom");
        owl.setImageUrl("/images/owl.jpg");

        when(characterService.findById("wise-owl")).thenReturn(Optional.of(owl));

        mockMvc.perform(get("/api/v1/characters/wise-owl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wise-owl"))
                .andExpect(jsonPath("$.name").value("Wise Owl"))
                .andExpect(jsonPath("$.species").value("Great Horned Owl"))
                .andExpect(jsonPath("$.traits", hasSize(3)))
                .andExpect(jsonPath("$.baseStory").value("You embody wisdom"))
                .andExpect(jsonPath("$.imageUrl").value("/images/owl.jpg"));
    }

    @Test
    void testGetCharacterByIdNotFound() throws Exception {
        when(characterService.findById("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/characters/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCharactersEmpty() throws Exception {
        when(characterService.getAllCharacters()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}