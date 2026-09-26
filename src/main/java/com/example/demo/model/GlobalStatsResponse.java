package com.example.demo.model;

/** Response body for GET /api/v1/global/stats. */
public class GlobalStatsResponse {

    private long inputTokens;
    private long outputTokens;

    public GlobalStatsResponse(long inputTokens, long outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }
}