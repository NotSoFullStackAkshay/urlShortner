package com.example.urlshortener.service;

import com.example.urlshortener.exception.AliasAlreadyExistsException;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Note: create() is intentionally NOT @Transactional so that a unique-constraint
 * violation surfaces right here (and can be retried) instead of at outer commit time.
 */
@Service
public class ShortUrlService {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final Set<String> RESERVED = Set.of("api", "actuator", "error", "h2-console");

    private final ShortUrlRepository repository;
    private final CodeGenerator codeGenerator;

    public ShortUrlService(ShortUrlRepository repository, CodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    public ShortUrl create(String rawUrl, String alias, Integer ttlDays) {
        String url = validate(rawUrl);
        Instant expiresAt = ttlDays == null ? null : Instant.now().plus(Duration.ofDays(ttlDays));

        if (alias != null) {
            if (RESERVED.contains(alias.toLowerCase()) || repository.existsByCode(alias)) {
                throw new AliasAlreadyExistsException(alias);
            }
            try {
                return repository.saveAndFlush(new ShortUrl(alias, url, expiresAt));
            } catch (DataIntegrityViolationException e) {
                throw new AliasAlreadyExistsException(alias);
            }
        }

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (repository.existsByCode(code)) {
                continue;
            }
            try {
                return repository.saveAndFlush(new ShortUrl(code, url, expiresAt));
            } catch (DataIntegrityViolationException e) {
                log.warn("Code collision on attempt {} for code {}", attempt, code);
            }
        }
        throw new IllegalStateException("Could not generate a unique short code");
    }

    public ShortUrl find(String code) {
        return repository.findByCode(code).orElseThrow(() -> new UrlNotFoundException(code));
    }

    /** Looks up the target, records the click, and returns the original URL. */
    public String resolveAndCount(String code) {
        ShortUrl shortUrl = find(code);
        if (shortUrl.isExpired()) {
            throw new UrlExpiredException(code);
        }
        repository.incrementClicks(shortUrl.getId());
        return shortUrl.getOriginalUrl();
    }

    public void delete(String code) {
        repository.delete(find(code));
    }

    private String validate(String rawUrl) {
        String url = rawUrl == null ? "" : rawUrl.trim();
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            boolean httpScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!httpScheme || uri.getHost() == null) {
                throw new InvalidUrlException("URL must be an absolute http or https URL");
            }
            return url;
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Malformed URL: " + e.getReason());
        }
    }
}
