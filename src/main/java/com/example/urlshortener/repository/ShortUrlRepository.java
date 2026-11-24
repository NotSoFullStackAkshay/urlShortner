package com.example.urlshortener.repository;

import com.example.urlshortener.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByCode(String code);

    boolean existsByCode(String code);

    /** Atomic increment, safe under concurrent clicks. */
    @Modifying
    @Transactional
    @Query("update ShortUrl s set s.clickCount = s.clickCount + 1 where s.id = :id")
    void incrementClicks(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("delete from ShortUrl s where s.expiresAt is not null and s.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
