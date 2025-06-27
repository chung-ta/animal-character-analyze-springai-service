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
public class Character {
    private String id;
    private String name;
    private String species;
    private List<String> traits;
    private String baseStory;
    private String imageUrl;
}