package com.example.demo.model;

import java.time.Instant;

/** Response body for GET /api/v1/admin/uptime. */
public class UptimeResponse {

    private Instant utcServerStart;
    private Instant utcNow;
    private double serverUptimeSeconds;

    public UptimeResponse(Instant utcServerStart, Instant utcNow, double serverUptimeSeconds) {
        this.utcServerStart = utcServerStart;
        this.utcNow = utcNow;
        this.serverUptimeSeconds = serverUptimeSeconds;
    }

    public Instant getUtcServerStart() {
        return utcServerStart;
    }

    public Instant getUtcNow() {
        return utcNow;
    }

    public double getServerUptimeSeconds() {
        return serverUptimeSeconds;
    }
}