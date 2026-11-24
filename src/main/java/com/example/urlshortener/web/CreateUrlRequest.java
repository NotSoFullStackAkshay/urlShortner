package com.example.urlshortener.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @NotBlank @Size(max = 2048) String url,
        @Pattern(regexp = "^[a-zA-Z0-9_-]{4,32}$",
                 message = "alias must be 4-32 chars: letters, digits, '-' or '_'") String alias,
        @Min(1) @Max(3650) Integer expiresInDays) {
}
