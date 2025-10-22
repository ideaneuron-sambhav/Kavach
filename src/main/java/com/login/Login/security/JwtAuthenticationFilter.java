package com.login.Login.security;

import com.login.Login.dto.Response;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/auth/login") ||
                path.startsWith("/auth/verify-otp") ||
                path.startsWith("springdoc.api-docs.path=/**")||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        try {
            if (header == null || !header.startsWith("Bearer ")) {
                // Allow /auth/** without token
                if (!request.getRequestURI().startsWith("/auth")) {
                    handleJwtError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
                    return;
                }

            } else {
                // After stripping "Bearer "
                String token = header.substring(7); // Strip "Bearer "

                // 1️⃣ Validate token structure
                jwtUtil.validateToken(token); // throws if malformed/expired

                // 2️⃣ Check if token is blacklisted
                if (jwtBlacklistService.isBlacklisted(token)) {
                    // Get the reason for blacklisting (logout, another login, etc.)
                    String reason = jwtBlacklistService.getBlacklistReason(token);
                    handleJwtError(response, HttpStatus.UNAUTHORIZED, reason);
                    return; // stop filter chain here
                }

                // 3️⃣ Continue with authentication if token is valid and not blacklisted
                String username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } catch (RuntimeException ex) {
            handleJwtError(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }



    private void handleJwtError(HttpServletResponse response, HttpStatus status,String message) throws IOException {
        Response<Object> errorResponse = Response.builder()
                .data(null)
                .httpStatusCode(status.value())
                .message(message)
                .build();

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.toJson(errorResponse));
    }

}
