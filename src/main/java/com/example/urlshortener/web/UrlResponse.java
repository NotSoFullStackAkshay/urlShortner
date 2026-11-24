package com.example.urlshortener.web;

import com.example.urlshortener.model.ShortUrl;

import java.time.Instant;

public record UrlResponse(
        String code,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt,
        long clickCount) {

    public static UrlResponse from(ShortUrl s, String shortUrl) {
        return new UrlResponse(s.getCode(), shortUrl, s.getOriginalUrl(),
                s.getCreatedAt(), s.getExpiresAt(), s.getClickCount());
    }
}
