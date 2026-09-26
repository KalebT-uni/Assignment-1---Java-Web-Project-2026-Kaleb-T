package com.example.demo.controller;

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

/**
 * Exposes the administration and statistics endpoints defined in the
 * assignment's YAML API specification: server uptime, cumulative token
 * usage, and graceful shutdown.
 */

@RestController
public class AdminController {

    private final ServerStatsService statsService;
    private final ApplicationContext applicationContext;

    public AdminController(ServerStatsService statsService, ApplicationContext applicationContext) {
        this.statsService = statsService;
        this.applicationContext = applicationContext;
    }
    
    /**
     * Reports how long the server has been running, computed from the
     * timestamp captured when {@link ServerStatsService} was constructed.
     */
    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getUptime() {
        Instant start = statsService.getStartTime();
        Instant now = Instant.now();
        double uptimeSeconds = Duration.between(start, now).toMillis() / 1000.0;
        return new UptimeResponse(start, now, uptimeSeconds);
    }
    
    /**
     * Reports cumulative input/output token usage from the transcription
     * Cloud service since the current server process started.
     */
    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getGlobalStats() {
        return new GlobalStatsResponse(statsService.getInputTokens(), statsService.getOutputTokens());
    }

    /**
     * Requests a graceful shutdown of the application. Returns 409 if a
     * shutdown is already in progress (guarded atomically via
     * {@link ServerStatsService#tryBeginShutdown()}), otherwise returns 202
     * and closes the Spring context on a separate thread so this response
     * can be written back to the client before the server actually stops.
     */
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
}