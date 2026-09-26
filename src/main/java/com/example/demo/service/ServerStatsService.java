package com.example.demo.service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

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

    public boolean tryBeginShutdown() {
        return shuttingDown.compareAndSet(false, true);
    }
}