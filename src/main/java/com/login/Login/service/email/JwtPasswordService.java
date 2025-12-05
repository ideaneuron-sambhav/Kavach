package com.login.Login.service.email;
import com.login.Login.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtPasswordService {

    @Autowired
    JwtUtil jwtUtil;
    private static final String SECRET_KEY = "your-secret-key-secret-ket-secret-key";
    private static final long PASSWORD_RESET_EXPIRATION_TIME = 86400000L; // 24 hours

    // Generate a JWT token for password reset
    public String generatePasswordResetToken(String email) {
        jwtUtil.ensureAdminFromContext();
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // Validate the token (checks for expiration as well)
    public boolean validatePasswordResetToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token); // this will throw an exception if the token is expired or invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract the user's email from the reset token
    public String getEmailFromResetToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
