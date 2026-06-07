package com.ssaika.ssiren.global.security;

import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiry}")
    private long accessExpiry;

    @Value("${jwt.refresh-expiry}")
    private long refreshExpiry;

    public String generateAccessToken(Long userId) {
        return buildToken(userId, accessExpiry);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, refreshExpiry);
    }

    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public void validateToken(String token) {
        parseClaims(token);
    }

    public long getRefreshExpiry() {
        return refreshExpiry;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED.getMessage(),
                ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN.getMessage(), ErrorCode.INVALID_TOKEN);
        }
    }

    private String buildToken(Long userId, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expiryMs))
            .signWith(getSigningKey())
            .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
