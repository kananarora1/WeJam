package com.example.wejam.host.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MediaLinkDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "https://youtu.be/abc123")
        @NotBlank @Size(max = 300) @Pattern(regexp = "https?://\\S+", message = "must be an http(s) link") String url,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true, example = "Live at Amber Room")
        @Size(max = 80) String title) {
}
