package com.example.demo.service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/**
 * Holds server-wide state shared across all concurrent requests: the
 * server's start time, cumulative token usage, and whether a shutdown is
 * in progress. As a Spring singleton bean, every request thread shares
 * this one instance, so all mutable state uses atomic types to remain
 * safe under concurrent access.
 */
@Service
public class ServerStatsService {

    private final Instant startTime = Instant.now();
    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public Instant getStartTime() {
        return startTime;
    }

    public void addInputTokens(long amount) {
        inputTokens.addAndGet(amount);
    }

    public void addOutputTokens(long amount) {
        outputTokens.addAndGet(amount);
    }

    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }
    
    /**
     * Atomically marks shutdown as started, returning true only for the
     * first caller. Prevents two concurrent shutdown requests from both
     * being accepted.
     */
    public boolean tryBeginShutdown() {
        return shuttingDown.compareAndSet(false, true);
    }
}