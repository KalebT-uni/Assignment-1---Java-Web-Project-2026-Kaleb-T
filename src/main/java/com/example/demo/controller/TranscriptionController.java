package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranscriptionController {

    @GetMapping("/api/v1/ping")
    public String ping() {
        return "pong";
    }
}