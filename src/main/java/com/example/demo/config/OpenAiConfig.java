package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the transcription API's credential and URL from environment
 * variables, allowing the same code to target either the real OpenAI
 * endpoint (used on TITAN, where OPENAI_API_KEY is injected) or a local
 * development adapter (by setting OPENAI_API_URL), without any code
 * changes between environments.
 */
@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}