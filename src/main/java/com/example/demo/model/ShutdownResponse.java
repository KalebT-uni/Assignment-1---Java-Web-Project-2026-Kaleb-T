package com.example.demo.model;

/** Response body for POST /api/v1/admin/shutdown on success. */
public class ShutdownResponse {

    private String message;

    public ShutdownResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}