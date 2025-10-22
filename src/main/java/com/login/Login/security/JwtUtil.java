package com.login.Login.security;

import com.login.Login.entity.User;
import com.login.Login.exception.TokenBlacklistedException;
import com.login.Login.exception.UserNotFoundException;
import com.login.Login.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60) )) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateToken(String token) {
        token = stripBearer(token);

        if (jwtBlacklistService.isBlacklisted(token)) {
            String reason = jwtBlacklistService.getBlacklistReason(token);
            throw new TokenBlacklistedException(reason);
        }

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            // Instead of throwing RuntimeException, just let caller decide
            throw new ExpiredJwtException(null, null, "Token expired"); // optional: catch separately in APIs
        } catch (MalformedJwtException | SignatureException e) {
            throw new RuntimeException("Invalid token signature");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    public long getExpirationMillisSafe(String token) {
        token = stripBearer(token);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .getTime();
        } catch (ExpiredJwtException e) {
            // token expired, return current time so it can be blacklisted immediately
            return System.currentTimeMillis();
        }
    }


    public String extractUsername(String token) {
        token = stripBearer(token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public User getUserFromToken(String token) throws UserNotFoundException {
        validateToken(token);
        String email = extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for token"));
    }

    public void ensureAdmin(String token) {
        validateToken(token);
        User user = getUserFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new RuntimeException("Access denied: Admin privileges required");
        }
    }
    public Date getExpirationDate(String token) {
        token = stripBearer(token);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
    public long getExpirationMillis(String token) {
        token = stripBearer(token);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }

    public User getAuthenticatedUserFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized access");
        }
        String email = auth.getName(); // this is usually set by your authentication filter
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    public void ensureAdminFromContext() {
        User user = getAuthenticatedUserFromContext();
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new RuntimeException("Access denied: Admin privileges required");
        }
    }


    private String stripBearer(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
