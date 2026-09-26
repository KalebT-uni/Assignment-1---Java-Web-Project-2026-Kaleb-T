package com.example.demo.service;

import java.net.InetSocketAddress;
import java.time.Duration;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.config.OpenAiConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;


/**
 * Sends recorded audio to a Cloud speech-to-text endpoint (OpenAI's
 * gpt-4o-mini-transcribe API in production, or a local whisper.cpp-backed
 * adapter during development — see openai.api.url) and returns the
 * transcribed text. Token usage from each response is recorded in
 * {@link ServerStatsService} for the /api/v1/global/stats endpoint.
 */
@Service
public class TranscriptionService {

    private final WebClient webClient;
    private final OpenAiConfig config;
    private final ServerStatsService statsService;

    public TranscriptionService(OpenAiConfig config, ServerStatsService statsService) {
        this.config = config;
        this.statsService = statsService;

        HttpClient httpClient = HttpClient.create()
                // Guards against a stalled request blocking indefinitely,
                // which would otherwise also block graceful shutdown.
                .responseTimeout(Duration.ofSeconds(20));
        
        // TITAN's network requires outbound traffic to go through a proxy,
        // injected as standard JVM system properties. Reactor Netty does
        // not honour these automatically, so they're applied explicitly
        // here. Locally, these properties are unset and this is skipped.
        String proxyHost = System.getProperty("https.proxyHost");
        String proxyPortStr = System.getProperty("https.proxyPort");

        if (proxyHost != null && !proxyHost.isBlank() && proxyPortStr != null) {
            int proxyPort = Integer.parseInt(proxyPortStr);
            httpClient = httpClient.proxy(proxySpec ->
            proxySpec.type(ProxyProvider.Proxy.HTTP)
                     .address(() -> new InetSocketAddress(proxyHost, proxyPort))
            );
        }

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    
    /**
     * Uploads the given audio bytes to the configured transcription
     * endpoint and returns the resulting text.
     *
     * @param audioBytes raw audio data, as recorded by the browser
     * @param filename   original filename, forwarded for the multipart request
     */
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
    
    /** Shape of the JSON response returned by the transcription endpoint. */
    private record TranscriptionResponse(String text, Usage usage) {}
    
    /**
     * Token usage counts from a single transcription call. Field names use
     * the snake_case JSON keys the API returns (input_tokens/output_tokens).
     */
    private record Usage(
        @JsonProperty("input_tokens") long inputTokens,
        @JsonProperty("output_tokens") long outputTokens
    ) {}
}