package com.example.demo.model;

/** Response body returned to the browser after a successful transcription. */
public class TranscriptionResultResponse {

    private String text;

    public TranscriptionResultResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}