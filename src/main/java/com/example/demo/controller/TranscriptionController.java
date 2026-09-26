package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.TranscriptionResultResponse;
import com.example.demo.service.TranscriptionService;

@RestController
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/api/v1/transcribe")
    public TranscriptionResultResponse transcribe(@RequestParam("audio") MultipartFile audioFile) throws Exception {
        byte[] audioBytes = audioFile.getBytes();
        String filename = audioFile.getOriginalFilename() != null ? audioFile.getOriginalFilename() : "recording.webm";
        String text = transcriptionService.transcribe(audioBytes, filename);
        return new TranscriptionResultResponse(text);
    }
}