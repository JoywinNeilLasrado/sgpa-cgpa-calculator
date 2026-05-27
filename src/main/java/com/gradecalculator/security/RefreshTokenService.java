package com.gradecalculator.security;

import com.gradecalculator.model.RefreshToken;
import com.gradecalculator.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Service managing JWT refresh token lifecycle:
 * generation, validation, rotation (one-time use), and revocation.
 *
 * <p>Security properties:
 * <ul>
 *   <li>Raw tokens are 256-bit random values encoded as Base64-URL</li>
 *   <li>Only the SHA-256 hash is persisted — plaintext never touches the DB</li>
 *   <li>Each refresh token is single-use (rotated on every refresh)</li>
 * </ul>
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Generate a new refresh token for the given user.
     *
     * @param userId    the user to issue the token for
     * @param ipAddress client IP (stored for audit)
     * @param userAgent client User-Agent (stored for audit)
     * @return the raw (plaintext) token to be sent to the client — stored nowhere
     */
    @Transactional
    public String generateRefreshToken(Long userId, String ipAddress, String userAgent) {
        String rawToken = generateRawToken();
        String hash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L);

        RefreshToken entity = new RefreshToken(hash, userId, expiresAt, userAgent, ipAddress);
        refreshTokenRepository.save(entity);

        log.debug("Issued refresh token for userId={}", userId);
        return rawToken;
    }

    /**
     * Validate the raw refresh token without consuming it.
     *
     * @return the stored entity if valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValid(String rawToken) {
        String hash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(hash)
                .filter(RefreshToken::isValid);
    }

    /**
     * Rotate: revoke the current refresh token and issue a new one.
     * Returns the new raw token. Throws if current token is invalid/expired.
     *
     * @param rawToken  the raw refresh token from the client
     * @param ipAddress client IP
     * @param userAgent client User-Agent
     * @return the new raw refresh token
     */
    @Transactional
    public String rotateRefreshToken(String rawToken, String ipAddress, String userAgent) {
        String hash = hashToken(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!existing.isValid()) {
            // Possible token reuse attack — revoke all tokens for this user
            log.warn("Security: Invalid/expired refresh token presented for userId={}. Revoking all tokens.", existing.getUserId());
            refreshTokenRepository.revokeAllByUserId(existing.getUserId());
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        // Revoke old token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Issue new token
        return generateRefreshToken(existing.getUserId(), ipAddress, userAgent);
    }

    /**
     * Revoke all active refresh tokens for a user (logout).
     *
     * @return the number of tokens revoked
     */
    @Transactional
    public int revokeAll(Long userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh token(s) for userId={}", count, userId);
        return count;
    }

    /**
     * Get userId from a valid raw refresh token.
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromToken(String rawToken) {
        return findValid(rawToken)
                .map(RefreshToken::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));
    }

    /**
     * Scheduled cleanup: delete expired and revoked tokens older than 1 day.
     * Runs every 6 hours.
     */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} expired/revoked refresh tokens", deleted);
        }
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String generateRawToken() {
        byte[] bytes = new byte[32]; // 256-bit
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 hash of a raw token, returned as lowercase hex.
     */
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
