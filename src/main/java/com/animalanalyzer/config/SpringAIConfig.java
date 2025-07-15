package com.animalanalyzer.config;

import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {
    
    @Value("${spring.ai.anthropic.chat.options.model:claude-3-opus-20240229}")
    private String model;
    
    @Value("${spring.ai.anthropic.chat.options.max-tokens:1500}")
    private Integer maxTokens;
    
    @Value("${spring.ai.anthropic.chat.options.temperature:0.7}")
    private Float temperature;
    
    @Value("${spring.ai.anthropic.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.anthropic.base-url:https://api.anthropic.com}")
    private String baseUrl;
    
    @Bean
    public AnthropicChatOptions anthropicChatOptions() {
        return AnthropicChatOptions.builder()
                .withModel(model)
                .withMaxTokens(maxTokens)
                .withTemperature(temperature)
                .build();
    }
    
    @Bean
    public AnthropicApi anthropicApi() {
        return new AnthropicApi(baseUrl, apiKey);
    }
}