package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.ErrorResponse;
import com.example.demo.model.GlobalStatsResponse;
import com.example.demo.model.ShutdownResponse;
import com.example.demo.model.UptimeResponse;
import com.example.demo.service.ServerStatsService;
import com.example.demo.service.TranscriptionService;

@RestController
public class AdminController {

    private final ServerStatsService statsService;
    private final ApplicationContext applicationContext;
    private final TranscriptionService transcriptionService;

    public AdminController(ServerStatsService statsService, ApplicationContext applicationContext, TranscriptionService transcriptionService) {
        this.statsService = statsService;
        this.applicationContext = applicationContext;
        this.transcriptionService = transcriptionService;
    }

    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getUptime() {
        Instant start = statsService.getStartTime();
        Instant now = Instant.now();
        double uptimeSeconds = Duration.between(start, now).toMillis() / 1000.0;
        return new UptimeResponse(start, now, uptimeSeconds);
    }

    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getGlobalStats() {
        return new GlobalStatsResponse(statsService.getInputTokens(), statsService.getOutputTokens());
    }

    @PostMapping("/api/v1/admin/shutdown")
    public ResponseEntity<?> shutdown() {
        if (!statsService.tryBeginShutdown()) {
            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    409,
                    "Conflict",
                    "Graceful shutdown is already in progress.",
                    "/api/v1/admin/shutdown"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        new Thread(() -> ((ConfigurableApplicationContext) applicationContext).close()).start();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ShutdownResponse("Graceful shutdown requested."));
    }

    @GetMapping("/api/v1/test-transcribe")
    public String testTranscribe() throws Exception {
        byte[] audioBytes = Files.readAllBytes(Paths.get("C:/Users/kaleb/OneDrive/Documents/Sound Recordings/test1.m4a"));
        return transcriptionService.transcribe(audioBytes, "test1.m4a");
    }
}