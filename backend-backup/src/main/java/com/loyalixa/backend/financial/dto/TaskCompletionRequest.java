package com.loyalixa.backend.financial.dto;
import jakarta.validation.constraints.NotBlank;
public record TaskCompletionRequest(
    @NotBlank(message = "Completion notes are required")
    String completionNotes
) {}
