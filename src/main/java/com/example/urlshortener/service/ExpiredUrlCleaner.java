package com.example.urlshortener.service;

import com.example.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExpiredUrlCleaner {

    private static final Logger log = LoggerFactory.getLogger(ExpiredUrlCleaner.class);
    private final ShortUrlRepository repository;

    public ExpiredUrlCleaner(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1M")
    public void purge() {
        int removed = repository.deleteExpired(Instant.now());
        if (removed > 0) {
            log.info("Removed {} expired short URLs", removed);
        }
    }
}
