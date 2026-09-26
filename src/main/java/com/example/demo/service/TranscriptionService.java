package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.config.OpenAiConfig;

@Service
public class TranscriptionService {

    private final WebClient webClient;
    private final OpenAiConfig config;
    private final ServerStatsService statsService;

    public TranscriptionService(OpenAiConfig config, ServerStatsService statsService) {
        this.config = config;
        this.statsService = statsService;
        this.webClient = WebClient.builder().build();
    }

    public String transcribe(byte[] audioBytes, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        builder.part("model", "gpt-4o-mini-transcribe");

        TranscriptionResponse response = webClient.post()
                .uri(config.getApiUrl())
                .header("Authorization", "Bearer " + config.getApiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(TranscriptionResponse.class)
                .block();

        if (response != null && response.usage() != null) {
            statsService.addInputTokens(response.usage().inputTokens());
            statsService.addOutputTokens(response.usage().outputTokens());
        }

        return response != null ? response.text() : "";
    }

    private record TranscriptionResponse(String text, Usage usage) {}

    private record Usage(
        @JsonProperty("input_tokens") long inputTokens,
        @JsonProperty("output_tokens") long outputTokens
    ) {}
}