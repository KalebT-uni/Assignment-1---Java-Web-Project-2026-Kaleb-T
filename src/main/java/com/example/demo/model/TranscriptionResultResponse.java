package com.example.demo.model;

public class TranscriptionResultResponse {

    private String text;

    public TranscriptionResultResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}