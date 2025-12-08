package com.login.Login.service.email;
import com.login.Login.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtPasswordService {

    @Autowired
    JwtUtil jwtUtil;
    private static final String SECRET_KEY = "your-secret-key-secret-key-secret-key";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long PASSWORD_RESET_EXPIRATION_TIME = 86400000L; // 24 hours

    // Generate a JWT token for password reset
    public String generatePasswordResetToken(String email) {
        jwtUtil.ensureAdminFromContext();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate the token (checks for expiration as well)
    public boolean validatePasswordResetToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract the user's email from the reset token
    public String getEmailFromResetToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
