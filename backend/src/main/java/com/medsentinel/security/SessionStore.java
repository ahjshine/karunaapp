package com.medsentinel.security;

import com.medsentinel.auth.AppUserDetails;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local opaque session store for the single-process demo server.
 * Tokens are random bearer values and are never derived from user data.
 */
public class SessionStore {

    private static final int TOKEN_BYTES = 32;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Duration ttl;

    public SessionStore() {
        this(readTtl());
    }

    public SessionStore(Duration ttl) {
        this.ttl = ttl == null || ttl.isZero() || ttl.isNegative() ? DEFAULT_TTL : ttl;
    }

    public String create(AppUserDetails principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Session principal is required");
        }

        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(tokenBytes);
        sessions.put(token, new Session(principal, Instant.now().plus(ttl)));
        return token;
    }

    public Optional<AppUserDetails> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (!session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(token, session);
            return Optional.empty();
        }
        return Optional.of(session.principal());
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public void invalidateUser(Long userId) {
        if (userId == null) {
            return;
        }
        sessions.entrySet().removeIf(entry -> userId.equals(entry.getValue().principal().getUserId()));
    }

    public long size() {
        return sessions.size();
    }

    public long ttlSeconds() {
        return ttl.toSeconds();
    }

    private static Duration readTtl() {
        String raw = System.getenv("MEDSENTINEL_SESSION_TTL_MINUTES");
        if (raw == null || raw.isBlank()) {
            return DEFAULT_TTL;
        }
        try {
            long minutes = Long.parseLong(raw.trim());
            if (minutes < 5 || minutes > 24 * 60) {
                return DEFAULT_TTL;
            }
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException ignored) {
            return DEFAULT_TTL;
        }
    }

    private record Session(AppUserDetails principal, Instant expiresAt) {}
}
