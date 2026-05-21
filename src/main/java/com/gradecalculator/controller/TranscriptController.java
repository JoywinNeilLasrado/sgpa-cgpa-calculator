package com.gradecalculator.controller;

import com.gradecalculator.service.TranscriptPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transcript PDF Controller
 */
@RestController
@RequestMapping("/api/transcript")
public class TranscriptController {

    private final TranscriptPdfService transcriptPdfService;

    public TranscriptController(TranscriptPdfService transcriptPdfService) {
        this.transcriptPdfService = transcriptPdfService;
    }

    /**
     * Download PDF transcript - GET /api/transcript/pdf/{studentId}
     */
    @GetMapping("/pdf/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTranscript(@PathVariable Long studentId) throws Exception {
        byte[] pdf = transcriptPdfService.generateTranscript(studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transcript-" + studentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}