package com.example.urlshortener.web;

import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UrlController {

    private final ShortUrlService service;

    public UrlController(ShortUrlService service) {
        this.service = service;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        ShortUrl created = service.create(request.url(), request.alias(), request.expiresInDays());
        UrlResponse body = toResponse(created);
        return ResponseEntity.created(URI.create(body.shortUrl())).body(body);
    }

    @GetMapping("/api/urls/{code}")
    public UrlResponse stats(@PathVariable String code) {
        return toResponse(service.find(code));
    }

    @DeleteMapping("/api/urls/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolveAndCount(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    private UrlResponse toResponse(ShortUrl s) {
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{code}").buildAndExpand(s.getCode()).toUriString();
        return UrlResponse.from(s, shortUrl);
    }
}
