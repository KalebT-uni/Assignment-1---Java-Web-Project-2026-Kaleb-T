package com.example.demo.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.ErrorResponse;
import com.example.demo.model.TranscriptionResultResponse;
import com.example.demo.service.TranscriptionService;

/**
 * Receives recorded audio uploaded from the browser's front end and
 * returns its transcribed text. The uploaded file is expected as a
 * multipart form field named "audio", matching the front end's
 * MediaRecorder/fetch implementation.
 */
@RestController
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/api/v1/transcribe")
    public ResponseEntity<?> transcribe(@RequestParam("audio") MultipartFile audioFile) {
        byte[] audioBytes;
        try {
            audioBytes = audioFile.getBytes();
        } catch (Exception e) {
            return errorResponse("Could not read the uploaded audio data.");
        }

        String filename = audioFile.getOriginalFilename() != null
                ? audioFile.getOriginalFilename()
                : "recording.webm";

        try {
            String text = transcriptionService.transcribe(audioBytes, filename);
            return ResponseEntity.ok(new TranscriptionResultResponse(text));
        } catch (Exception e) {
            return errorResponse("Transcription failed: the speech-to-text service could not be reached or returned an error.");
        }
    }

    /**
     * Builds a 500 response using the assignment's standard error shape,
     * so failures here are structured the same way as the admin API's errors.
     */
    private ResponseEntity<ErrorResponse> errorResponse(String message) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                500,
                "Internal Server Error",
                message,
                "/api/v1/transcribe"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}